# API Endpoints Summary

This document summarizes the main REST endpoints exposed under `/api/**`. Swagger UI is available at `/swagger-ui/index.html` when the app is running.

## Authentication

The API uses stateless JWT bearer authentication.

- `POST /api/auth/register` and `POST /api/auth/login` are public.
- Other `/api/**` endpoints require `Authorization: Bearer <accessToken>` unless noted otherwise.
- Browser pages use Spring form login, then call `GET /auth/access-token` to mint a JWT for same-origin API calls.
- Refresh tokens are not implemented.

## Auth

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a new user account |
| `POST` | `/api/auth/login` | Public | Authenticate and return a JWT access token |
| `POST` | `/api/auth/logout` | JWT | Stateless logout acknowledgement; client discards token |
| `GET` | `/api/auth/me` | JWT | Return the authenticated user |
| `GET` | `/auth/access-token` | Web session | Mint a JWT for authenticated browser pages |

## Urge Logs

All urge-log endpoints are scoped to the authenticated `ROLE_USER`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/urge-logs` | Create an urge log |
| `GET` | `/api/urge-logs` | List owned urge logs newest first |
| `GET` | `/api/urge-logs/{id}` | Read one owned urge log |
| `PUT` | `/api/urge-logs/{id}` | Update one owned urge log |
| `DELETE` | `/api/urge-logs/{id}` | Delete one owned urge log |

## Exposure Tasks

All exposure-task endpoints are scoped to the authenticated `ROLE_USER`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/exposure-tasks` | Create an exposure task |
| `GET` | `/api/exposure-tasks` | List owned exposure tasks |
| `GET` | `/api/exposure-tasks/{id}` | Read one owned exposure task |
| `PUT` | `/api/exposure-tasks/{id}` | Update one owned exposure task |
| `PATCH` | `/api/exposure-tasks/{id}/start` | Mark task in progress |
| `PATCH` | `/api/exposure-tasks/{id}/complete` | Mark task completed |
| `PATCH` | `/api/exposure-tasks/{id}/skip` | Mark task skipped |
| `DELETE` | `/api/exposure-tasks/{id}` | Delete one owned exposure task |

## Compulsion Delay Attempts

All delay-attempt endpoints are scoped to the authenticated `ROLE_USER`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/compulsion-delay-attempts` | Create a delay attempt |
| `GET` | `/api/compulsion-delay-attempts` | List owned delay attempts |
| `GET` | `/api/compulsion-delay-attempts/{id}` | Read one owned delay attempt |
| `PUT` | `/api/compulsion-delay-attempts/{id}` | Update one owned delay attempt |
| `PATCH` | `/api/compulsion-delay-attempts/{id}/complete` | Complete an open delay attempt |
| `PATCH` | `/api/compulsion-delay-attempts/{id}/cancel` | Cancel an open delay attempt |
| `DELETE` | `/api/compulsion-delay-attempts/{id}` | Delete one owned delay attempt |

## AI Coach

These endpoints allow `ROLE_USER` and `ROLE_THERAPIST`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/ai/coach` | Send a user message through the guarded AI coach pipeline |
| `POST` | `/api/ai/conversations/new` | Close the current active conversation and return a summary when available |
| `GET` | `/api/ai/conversations/{conversationId}/messages` | List messages for an owned conversation |
| `GET` | `/api/ai/weekly-summary` | Return a weekly AI-assisted summary |

Therapists can see AI safety metadata for assigned-patient review. Regular users receive user-facing coach responses with clinical routing metadata hidden.

## Progress and Reports

These endpoints require `ROLE_USER`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/progress/analytics` | Return dashboard analytics |
| `GET` | `/api/reports/progress` | Return the last seven days of progress as JSON |
| `GET` | `/api/reports/progress/download` | Download an HTML progress report |

## Admin

These endpoints require `ROLE_ADMIN`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/admin/users` | List users |
| `POST` | `/api/admin/assign-therapist?therapistId={id}&userId={id}` | Assign a therapist to a user |

## Therapist

These endpoints require `ROLE_THERAPIST`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/therapist/patients` | List assigned patients |
| `GET` | `/api/therapist/patients/{patientId}/ai/conversations/{conversationId}/messages` | Read assigned-patient AI messages |
