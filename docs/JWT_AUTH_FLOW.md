# JWT Authentication Flow

This document identifies where JWT bearer tokens are attached to outgoing requests in this repository and explains the current refresh-token behavior.

## Where JWTs are attached to outgoing requests

Production browser API requests now attach JWT access tokens through a shared browser wrapper:

| File | What happens |
| --- | --- |
| `src/main/resources/static/js/steady-api.js` | Defines `window.steadyApiFetch(...)`, lazily loads an access token, and attaches `Authorization: Bearer <accessToken>` to same-origin `/api/**` requests. |
| `src/main/resources/templates/fragments/navbar.html` | Loads `/js/steady-api.js` for authenticated Thymeleaf pages that include the shared navbar. |
| `src/main/resources/templates/dashboard.html` | Uses `window.steadyApiFetch(...)` for the dashboard delay-attempt API request. |
| `src/main/resources/templates/ai-coach.html` | Uses `window.steadyApiFetch(...)` for AI coach and AI conversation API requests. |
| `src/main/resources/templates/delay-attempts.html` | Uses `window.steadyApiFetch(...)` for delay-attempt API requests. |
| `src/main/resources/templates/exposure-list.html` | Uses `window.steadyApiFetch(...)` for exposure-task API requests. |
| `src/main/resources/templates/progress.html` | Uses `window.steadyApiFetch(...)` for progress analytics API requests. |
| `src/main/resources/templates/therapist-patient-detail.html` | Uses `window.steadyApiFetch(...)` for therapist AI conversation API requests. |
| `src/main/resources/templates/urge-log-list.html` | Uses `window.steadyApiFetch(...)` for urge-log API requests. |
| `src/main/resources/templates/weekly-summary.html` | Uses `window.steadyApiFetch(...)` for weekly summary API requests. |

The wrapper only attaches the JWT to same-origin URLs whose path starts with `/api/`. It does not attach the JWT to external URLs.

The integration tests also attach bearer tokens directly in `src/test/java/com/stark/steadyai/security/ApiSecurityIntegrationTest.java` to verify stateless API authorization.

## Browser token bootstrap flow

The browser UI still uses Spring form login and web sessions for server-rendered Thymeleaf pages. To call stateless `/api/**` endpoints from those pages, the app now bridges the authenticated web session to a short-lived JWT access token.

`src/main/java/com/stark/steadyai/controller/AccessTokenController.java` exposes:

```http
GET /auth/access-token
```

That endpoint is outside `/api/**`, so it is protected by the normal web security chain. It requires an authenticated web session, uses `JwtTokenService.generateToken(...)` for the current `CustomUserDetails`, and returns:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresAt": "2026-06-14T12:34:56Z"
}
```

The response is sent with `Cache-Control: no-store`. The token is returned in the JSON body only; the endpoint does not set a token cookie.

`window.steadyApiFetch(...)` in `steady-api.js` implements the browser-side logic:

1. For non-API or cross-origin requests, it delegates to native `fetch(...)` unchanged.
2. For same-origin `/api/**` requests, it loads an access token from `/auth/access-token` using `credentials: "same-origin"` and `cache: "no-store"`.
3. It stores the token only in JavaScript memory.
4. It re-mints the token when there is no token, when the token is expired, or when the token has less than 60 seconds remaining.
5. It sends the API request with `Authorization: Bearer <accessToken>`.
6. If the API returns `401`, it clears the cached token, fetches a new one once, and retries the original request once.
7. If token bootstrap fails, it redirects the browser to `/login`.

## Access-token issuing flow

There are now two ways to receive an access token:

1. REST/API clients call `POST /api/auth/login`.
2. Authenticated browser pages call `GET /auth/access-token` through the shared wrapper.

Both paths use `src/main/java/com/stark/steadyai/security/JwtTokenService.java`.

The token payload is built as follows:

1. `issuedAt` is set to `Instant.now()`.
2. `expiresAt` is calculated with `jwtProperties.getAccessTokenTtl()`.
3. Claims include issuer, issued-at time, expiry, subject, user ID, email, name, and role.
4. The token is signed with HS256 through Spring Security's `JwtEncoder`.
5. The resulting token string and expiry timestamp are returned as a `GeneratedJwtToken`.

The access-token TTL is configured by `security.jwt.access-token-ttl`. In `src/main/resources/application.properties`, it is currently `15m`. The default in `src/main/java/com/stark/steadyai/config/JwtProperties.java` is also 15 minutes.

## Current API validation flow

JWT validation is configured in `src/main/java/com/stark/steadyai/config/SecurityConfig.java`.

The `/api/**` security filter chain is stateless:

1. It matches only `/api/**`.
2. CSRF, form login, HTTP Basic, logout, and request caching are disabled for API requests.
3. Session creation policy is `STATELESS`.
4. `/api/auth/register` and `/api/auth/login` are public.
5. Other API endpoints require authentication and role-based authorization.
6. Spring Security's OAuth2 resource server support validates incoming bearer JWTs.

The decoder uses the configured HS256 secret and validates the token issuer. The custom JWT authentication converter loads the user by the JWT subject and maps the JWT `role` claim to the request authorities.

## Refresh-token handling

Refresh tokens are not implemented in the current repository.

The browser session bridge is not a refresh-token flow. It mints a new short-lived access token from an already authenticated Spring web session. There is still no long-lived refresh-token credential, no refresh-token persistence, no refresh endpoint, and no refresh-token rotation or revocation logic.

Evidence:

1. `LoginResponse` contains only `accessToken`, `tokenType`, `expiresAt`, and `user`.
2. `AccessTokenResponse` contains only `accessToken`, `tokenType`, and `expiresAt`.
3. There is no `refreshToken` field, DTO, entity, repository, service, or database table.
4. There is no endpoint such as `/api/auth/refresh` or `/api/auth/revoke` for refresh-token exchange or revocation. `POST /api/auth/logout` is a stateless acknowledgement that tells the client to discard its bearer token.

Because refresh tokens do not exist here, standalone API clients must re-authenticate with credentials after access-token expiry. Browser pages can obtain another access token only while their normal Spring web session remains authenticated.
