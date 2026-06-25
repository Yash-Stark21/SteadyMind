# SteadyMind

SteadyMind is a compassionate, self-guided web application for tracking urges, practicing compulsion delay, planning exposure tasks, reviewing progress, and using a controlled AI wellness coach for safe reflection.

This project is for demonstration and educational purposes only. It is not a medical device, therapy replacement, diagnostic tool, or crisis-support system.

## Problem Statement

Living with strong urges or compulsions, including experiences related to OCD, anxiety, and habit loops, can be overwhelming. Support is not always available at the moment of high anxiety. SteadyMind provides a private, structured way to log difficult moments, practice delaying compulsions, and review patterns over time.

## Key Features

- **Moment logs:** Track triggers, obsessions, compulsion urges, delay minutes, and anxiety intensity before and after.
- **Delay attempts:** Practice sitting with discomfort using planned delay windows, coping strategies, outcomes, and notes.
- **Exposure tasks:** Create growth challenges, update difficulty, and move tasks through pending, in-progress, completed, and skipped states.
- **Progress analytics:** Review seven-day urge trends, trigger breakdowns, intensity distribution, delay metrics, and progress observations.
- **Progress reports:** Generate JSON or downloadable HTML summaries for the last seven days.
- **AI wellness coach:** Uses a guarded AI pipeline for non-diagnostic reflection, crisis routing, reassurance-seeking redirects, and weekly summaries.
- **Role-based views:** Supports regular users, admins, and therapists with separate API permissions.

## Technical Specifications

| Area | Specification |
| --- | --- |
| Language/runtime | Java 25 |
| Build tool | Maven wrapper (`mvnw`, `mvnw.cmd`) |
| Application framework | Spring Boot 4.0.6 |
| Web stack | Spring MVC REST controllers plus Thymeleaf server-rendered pages |
| Security | Spring Security, stateless JWT bearer auth for `/api/**`, BCrypt password hashing |
| JWT signing | HS256 using `security.jwt.secret`; issuer `steadyai`; default access-token TTL `15m` |
| Persistence | Spring Data JPA, Hibernate, MySQL runtime configuration, H2 for local demo and tests |
| Database migration mode | Hibernate `spring.jpa.hibernate.ddl-auto` configured per profile/environment |
| API documentation | springdoc OpenAPI UI (`springdoc-openapi-starter-webmvc-ui` 3.0.3) |
| AI integration | Spring AI 2.0.0-M6 with OpenAI profile and deterministic mock profile |
| Frontend assets | HTML5, Thymeleaf, Bootstrap 5.3, vanilla CSS, Chart.js |
| Testing | Spring Boot Test, JUnit Jupiter, Mockito, Spring Security Test, H2 test database |
| API media types | JSON for REST APIs; progress-report download returns HTML attachment |

## Architecture Overview

The codebase follows a layered Spring Boot architecture:

- **Controllers:** Handle HTTP routing for REST APIs and Thymeleaf MVC pages.
- **DTOs:** Define request and response contracts exposed by the REST API.
- **Services:** Own business rules, user scoping, analytics, report generation, and AI safety orchestration.
- **Repositories:** Spring Data JPA repositories for persistence queries.
- **Entities:** Model users, urge logs, exposure tasks, delay attempts, AI conversations/messages, and therapist assignments.
- **Security:** Separate filter chains for stateless `/api/**` JWT traffic and session-backed web pages.

## REST API and Swagger/OpenAPI

The REST API is available under `http://localhost:8080` when the application is running.

| Tool | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| Swagger UI redirect | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

OpenAPI is configured in `src/main/java/com/stark/steadyai/config/OpenApiConfig.java` with:

- Title: `SteadyMind API`
- Version: `1.0.0`
- Description: `REST API documentation for SteadyMind Application`
- Security scheme: `bearerAuth`
- Auth type: HTTP bearer token
- Bearer format: JWT

Swagger UI and `/v3/api-docs/**` are public through the web security chain. Most `/api/**` operations still require a JWT when executed.

To use protected endpoints in Swagger UI:

1. Start the application.
2. Call `POST /api/auth/login` with a seeded or registered account.
3. Copy the `accessToken` from the response.
4. Click **Authorize** in Swagger UI and provide the JWT value.
5. For raw HTTP clients, send `Authorization: Bearer <accessToken>`.

Export the generated OpenAPI document with PowerShell:

```powershell
Invoke-WebRequest http://localhost:8080/v3/api-docs -OutFile openapi.json
```

## Authentication and Authorization

The API uses stateless JWT authentication for `/api/**`.

- `POST /api/auth/register` and `POST /api/auth/login` are public.
- All other `/api/**` endpoints require `Authorization: Bearer <accessToken>` unless explicitly listed as public.
- `/api/admin/**` requires `ROLE_ADMIN`.
- `/api/therapist/**` requires `ROLE_THERAPIST`.
- `/api/ai/**` allows `ROLE_USER` and `ROLE_THERAPIST`.
- Other `/api/**` endpoints require `ROLE_USER`.
- Browser-rendered pages use Spring form login sessions, then call `GET /auth/access-token` to mint a short-lived JWT for same-origin API requests.

Seeded users are created by `DataSeeder` if they do not already exist:

| Email | Password | Role |
| --- | --- | --- |
| `user@example.com` | `password123` | `ROLE_USER` |
| `admin@example.com` | `password123` | `ROLE_ADMIN` |
| `therapist@example.com` | `password123` | `ROLE_THERAPIST` |
| `demo@steadyai.local` | `password123` | `ROLE_USER` |

These accounts are demo credentials for local development and portfolio review only.

JWT payloads are generated by `JwtTokenService` and include issuer, subject, issued-at time, expiry, user ID, email, name, and role. Refresh tokens are not implemented; standalone API clients should log in again after access-token expiry.

## REST Endpoint Catalog

### Auth

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a new `ROLE_USER` account. |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT access token. |
| `POST` | `/api/auth/logout` | JWT | Stateless logout acknowledgement; client discards token. |
| `GET` | `/api/auth/me` | JWT | Return the authenticated user profile. |
| `GET` | `/auth/access-token` | Web session | Mint a JWT for authenticated Thymeleaf pages; returns `Cache-Control: no-store`. |

### Urge Logs

All urge-log endpoints are scoped to the authenticated `ROLE_USER`.

| Method | Path | Response |
| --- | --- | --- |
| `POST` | `/api/urge-logs` | `201 Created`, `UrgeLogResponse` |
| `GET` | `/api/urge-logs` | `200 OK`, list of `UrgeLogResponse` ordered newest first |
| `GET` | `/api/urge-logs/{id}` | `200 OK`, one owned `UrgeLogResponse` |
| `PUT` | `/api/urge-logs/{id}` | `200 OK`, updated `UrgeLogResponse` |
| `DELETE` | `/api/urge-logs/{id}` | `204 No Content` |

### Exposure Tasks

All exposure-task endpoints are scoped to the authenticated `ROLE_USER`.

| Method | Path | Response |
| --- | --- | --- |
| `POST` | `/api/exposure-tasks` | `201 Created`, `ExposureTaskResponse` |
| `GET` | `/api/exposure-tasks` | `200 OK`, list of `ExposureTaskResponse` ordered by difficulty |
| `GET` | `/api/exposure-tasks/{id}` | `200 OK`, one owned `ExposureTaskResponse` |
| `PUT` | `/api/exposure-tasks/{id}` | `200 OK`, updated `ExposureTaskResponse` |
| `PATCH` | `/api/exposure-tasks/{id}/start` | `200 OK`, status changed to `IN_PROGRESS` |
| `PATCH` | `/api/exposure-tasks/{id}/complete` | `200 OK`, status changed to `COMPLETED` and `completedAt` set |
| `PATCH` | `/api/exposure-tasks/{id}/skip` | `200 OK`, status changed to `SKIPPED` |
| `DELETE` | `/api/exposure-tasks/{id}` | `204 No Content` |

### Compulsion Delay Attempts

All delay-attempt endpoints are scoped to the authenticated `ROLE_USER`.

| Method | Path | Response |
| --- | --- | --- |
| `POST` | `/api/compulsion-delay-attempts` | `201 Created`, `CompulsionDelayAttemptResponse` |
| `GET` | `/api/compulsion-delay-attempts` | `200 OK`, list of `CompulsionDelayAttemptResponse` ordered newest first |
| `GET` | `/api/compulsion-delay-attempts/{id}` | `200 OK`, one owned `CompulsionDelayAttemptResponse` |
| `PUT` | `/api/compulsion-delay-attempts/{id}` | `200 OK`, updated `CompulsionDelayAttemptResponse` |
| `PATCH` | `/api/compulsion-delay-attempts/{id}/complete` | `200 OK`, sets outcome, actual delay, and `endedAt`; returns `409` if already closed |
| `PATCH` | `/api/compulsion-delay-attempts/{id}/cancel` | `200 OK`, sets outcome to `CANCELLED`; returns `409` if already closed |
| `DELETE` | `/api/compulsion-delay-attempts/{id}` | `204 No Content` |

### AI Coach and Weekly Summary

These endpoints allow `ROLE_USER` and `ROLE_THERAPIST`.

| Method | Path | Response |
| --- | --- | --- |
| `POST` | `/api/ai/coach` | `200 OK`, `AiCoachExchangeResponseDto` |
| `POST` | `/api/ai/conversations/new` | `200 OK` with `AiConversationSummaryDto`, or `204 No Content` if no active conversation exists |
| `GET` | `/api/ai/conversations/{conversationId}/messages` | `200 OK`, list of `AiCoachExchangeResponseDto` |
| `GET` | `/api/ai/weekly-summary` | `200 OK`, `WeeklySummaryResponse` |

For non-therapist users, AI response metadata fields `intent`, `riskLevel`, and `responseType` are hidden as `null`. Therapists can see the full AI-safety metadata.

### Progress and Reports

These endpoints require `ROLE_USER`.

| Method | Path | Response |
| --- | --- | --- |
| `GET` | `/api/progress/analytics` | `200 OK`, `ProgressAnalyticsResponse` |
| `GET` | `/api/reports/progress` | `200 OK`, `ProgressReportResponse` for the last seven days |
| `GET` | `/api/reports/progress/download` | `200 OK`, `text/html` attachment named `steadyai-progress-report-<date>.html` |

### Admin

These endpoints require `ROLE_ADMIN`.

| Method | Path | Response |
| --- | --- | --- |
| `GET` | `/api/admin/users` | `200 OK`, list of users with `id`, `name`, `email`, and `role` |
| `POST` | `/api/admin/assign-therapist?therapistId={id}&userId={id}` | `200 OK` on assignment, `400 Bad Request` for invalid or duplicate assignment |

### Therapist

These endpoints require `ROLE_THERAPIST`.

| Method | Path | Response |
| --- | --- | --- |
| `GET` | `/api/therapist/patients` | `200 OK`, assigned patients with `id`, `name`, and `email` |
| `GET` | `/api/therapist/patients/{patientId}/ai/conversations/{conversationId}/messages` | `200 OK`, AI messages for an assigned patient conversation |

## Request DTO Specifications

| DTO | Fields and validation |
| --- | --- |
| `RegisterRequest` | `name` required; `email` required and valid email; `password` required, minimum 6 characters. |
| `LoginRequest` | `email` required and valid email; `password` required. |
| `UrgeLogRequest` | `triggerText` required, max 500; `obsessionText` optional, max 500; `compulsionUrge` required, max 300; `intensityBefore` required, 1-10; `delayMinutes` required and one of `0`, `2`, `5`, `10`, `15`; `intensityAfter` optional, 1-10; `compulsionPerformed` required. |
| `ExposureTaskRequest` | `title` required; `description` optional; `difficultyLevel` required; `targetDate` optional `LocalDateTime`. |
| `CompulsionDelayAttemptRequest` | `urgeLogId` optional; `exposureTaskId` optional; `triggerDescription` optional, max 500; `plannedDelayMinutes` positive when supplied; `copingStrategyUsed` optional; `notes` optional. Linked urge logs and exposure tasks must belong to the current user. |
| `CompulsionDelayAttemptUpdateRequest` | `triggerDescription` optional, max 500; `plannedDelayMinutes` positive when supplied; `copingStrategyUsed` optional; `notes` optional. |
| `CompleteDelayAttemptRequest` | `actualDelayMinutes` optional, minimum 0 when supplied; `outcome` required. |
| `AiCoachRequestDto` | `message` required, max 1000 characters. |

## Response DTO Specifications

| DTO | Fields |
| --- | --- |
| `AccessTokenResponse` | `accessToken`, `tokenType`, `expiresAt` |
| `LoginResponse` | `accessToken`, `tokenType`, `expiresAt`, `user` |
| `AuthenticatedUserResponse` | `id`, `name`, `email`, `role` |
| `UrgeLogResponse` | `id`, `triggerText`, `obsessionText`, `compulsionUrge`, `intensityBefore`, `delayMinutes`, `intensityAfter`, `compulsionPerformed`, `createdAt` |
| `ExposureTaskResponse` | `id`, `title`, `description`, `difficultyLevel`, `status`, `targetDate`, `completedAt`, `createdAt`, `updatedAt` |
| `CompulsionDelayAttemptResponse` | `id`, `urgeLogId`, `exposureTaskId`, `triggerDescription`, `plannedDelayMinutes`, `actualDelayMinutes`, `outcome`, `copingStrategyUsed`, `notes`, `startedAt`, `endedAt`, `createdAt`, `updatedAt` |
| `AiCoachExchangeResponseDto` | `conversationId`, `userMessage`, `aiResponse`, `intent`, `riskLevel`, `responseType`, `createdAt` |
| `AiConversationSummaryDto` | `conversationId`, `firstQuestion`, `endedAt` |
| `WeeklySummaryResponse` | `startDate`, `endDate`, `totalUrgeLogs`, `averageIntensity`, `mostCommonTrigger`, `highestRiskPeriod`, `progressObservations`, `recurringPatterns`, `suggestedNextSteps`, `safetyNote` |
| `ProgressAnalyticsResponse` | totals for logs, exposures, and delays; `sevenDayUrgeTrend`; `triggerBreakdown`; `intensityDistribution`; `progressObservations`; `safetyNote` |
| `ProgressReportResponse` | report dates, display name, urge/delay/exposure metrics, weekly summary, observations, suggested next steps, safety note |
| `AdminController.UserDto` | `id`, `name`, `email`, `role` |
| `TherapistController.PatientDto` | `id`, `name`, `email` |

## Enum Values

| Enum | Values |
| --- | --- |
| `Role` | `ROLE_USER`, `ROLE_ADMIN`, `ROLE_THERAPIST` |
| `ExposureDifficulty` | `LOW`, `MEDIUM`, `HIGH`, `EXTREME` |
| `ExposureStatus` | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `SKIPPED`, `PLANNED` |
| `CopingStrategy` | `BREATHING`, `WALKING`, `JOURNALING`, `COLD_WATER`, `CALL_SUPPORT`, `DISTRACTION`, `DELAY_TIMER`, `OTHER` |
| `CompulsionDelayOutcome` | `SUCCESS`, `PARTIAL_SUCCESS`, `FAILED`, `CANCELLED` |
| `CoachIntent` | `URGE_SUPPORT`, `REASSURANCE_SEEKING`, `EXPOSURE_REFLECTION`, `GENERAL_EDUCATION`, `CRISIS_OR_SELF_HARM`, `WEEKLY_SUMMARY`, `OUT_OF_SCOPE` |
| `RiskLevel` | `LOW`, `MEDIUM`, `HIGH` |
| `ResponseType` | `ERP_REDIRECT`, `REFLECTION_PROMPT`, `EDUCATIONAL`, `CRISIS_STATIC_MESSAGE`, `OUT_OF_SCOPE_MESSAGE` |
| `SuggestedAction` | `START_DELAY_TIMER`, `LOG_URGE`, `VIEW_EXPOSURE_LIST`, `CONTACT_SUPPORT`, `NONE` |

## Error Responses

Common API error shapes:

```json
{
  "timestamp": "2026-06-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "validationErrors": {
    "fieldName": "Validation message"
  }
}
```

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required"
}
```

| Status | Source |
| --- | --- |
| `400 Bad Request` | Bean validation failures, duplicate registration email, invalid therapist assignment, invalid login payload. |
| `401 Unauthorized` | Missing/invalid JWT or invalid login credentials. |
| `403 Forbidden` | Authenticated user lacks the required role. |
| `404 Not Found` | User-scoped resource does not exist or does not belong to the requester. |
| `409 Conflict` | Invalid delay-attempt state transition, such as completing an already closed attempt. |

## Configuration

Important application properties:

| Property | Purpose |
| --- | --- |
| `server.port=8080` | Local HTTP port. |
| `security.jwt.secret=${STEADYAI_JWT_SECRET:...}` | HS256 signing secret. Use a strong value of at least 32 bytes outside local demos. |
| `security.jwt.access-token-ttl=15m` | JWT access-token lifetime. |
| `security.jwt.issuer=steadyai` | JWT issuer validated by the resource server. |
| `spring.datasource.url=${STEADYAI_DATASOURCE_URL:...}` | MySQL JDBC URL for runtime persistence when not using the demo profile. |
| `spring.datasource.username=${STEADYAI_DATASOURCE_USERNAME:...}` | Runtime database username. |
| `spring.datasource.password=${STEADYAI_DATASOURCE_PASSWORD:}` | Runtime database password. No local password is committed. |
| `spring.jpa.hibernate.ddl-auto=${STEADYAI_JPA_DDL_AUTO:update}` | Hibernate schema update behavior. The demo profile overrides this to `create-drop`. |
| `spring.jpa.show-sql=${STEADYAI_JPA_SHOW_SQL:false}` | SQL logging flag. |
| `spring.ai.openai.api-key` | Reads `OPENAI_API_KEY`, falling back to `SPRING_AI_OPENAI_API_KEY`. |
| `spring.ai.openai.chat.model=gpt-5` | OpenAI chat model used by the OpenAI profile. |
| `spring.ai.openai.chat.temperature=0` | Deterministic model temperature. |

Profiles included in `src/main/resources`:

- `demo`: uses an in-memory H2 database for quick local review without MySQL.
- `openai`: uses Spring AI OpenAI configuration and requires an API key.
- `mock-ai`: uses `MockAiClient` for deterministic local responses without network access.
- `test`: uses H2 and test-focused settings from `src/test/resources/application-test.properties`.

## Setup Instructions

Prerequisites:

- JDK 25
- PowerShell on Windows, or a shell capable of running the Maven wrapper

Run the fastest local demo with H2 and deterministic mock AI:

```powershell
$env:SPRING_PROFILES_ACTIVE="demo,mock-ai"
$env:STEADYAI_JWT_SECRET="replace-with-a-strong-32-byte-minimum-secret"
.\mvnw.cmd spring-boot:run
```

Run with MySQL and deterministic mock AI:

```powershell
$env:SPRING_PROFILES_ACTIVE="mock-ai"
$env:STEADYAI_JWT_SECRET="replace-with-a-strong-32-byte-minimum-secret"
$env:STEADYAI_DATASOURCE_URL="jdbc:mysql://localhost:3306/steadyai?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:STEADYAI_DATASOURCE_USERNAME="steadyai"
$env:STEADYAI_DATASOURCE_PASSWORD="your_database_password"
.\mvnw.cmd spring-boot:run
```

Run with MySQL and OpenAI-backed responses:

```powershell
$env:SPRING_PROFILES_ACTIVE="openai"
$env:STEADYAI_JWT_SECRET="replace-with-a-strong-32-byte-minimum-secret"
$env:STEADYAI_DATASOURCE_URL="jdbc:mysql://localhost:3306/steadyai?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:STEADYAI_DATASOURCE_USERNAME="steadyai"
$env:STEADYAI_DATASOURCE_PASSWORD="your_database_password"
$env:OPENAI_API_KEY="your_api_key_here"
.\mvnw.cmd spring-boot:run
```

`.env.example` contains the same variables with safe placeholder values.

Access the app at:

```text
http://localhost:8080
```

Access Swagger UI at:

```text
http://localhost:8080/swagger-ui/index.html
```

## API Examples

Login:

```powershell
$login = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"email":"user@example.com","password":"password123"}'

$token = $login.accessToken
```

Create an urge log:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/urge-logs `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body '{
    "triggerText": "Touched a public door handle",
    "obsessionText": "What if I spread germs?",
    "compulsionUrge": "Wash hands repeatedly",
    "intensityBefore": 8,
    "delayMinutes": 5,
    "intensityAfter": 4,
    "compulsionPerformed": false
  }'
```

Send an AI coach message:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/ai/coach `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body '{"message":"I feel an urge to check again. What can I reflect on before acting?"}'
```

## Postman

The complete local Postman collection is included at:

```text
postman/collections/SteadyAI - Complete REST API.postman_collection.json
```

When present, the collection uses `{{baseUrl}}` and `{{accessToken}}`, includes login flows for the seeded roles, and covers the core REST endpoints. The generated OpenAPI document at `/v3/api-docs` remains the canonical REST API contract.

## Testing

Run the automated test suite:

```powershell
.\mvnw.cmd test
```

For a clean Maven test run:

```powershell
.\mvnw.cmd clean test
```

The test profile uses H2 and mock AI configuration so tests do not need a real OpenAI API key.

Latest verified local result: `53 tests, 0 failures, 0 errors, 0 skipped`.

## Manual Demo Flow

1. Navigate to `http://localhost:8080`.
2. Log in as `user@example.com` with password `password123`.
3. Open the dashboard and review progress cards.
4. Create an urge log from **Log a Moment**.
5. Start or record a delay attempt.
6. Add an exposure task and update its status.
7. Ask the AI Coach a reflection question.
8. Visit Progress Analytics and download the progress report.
9. Open Swagger UI and test the REST API using the JWT from `/api/auth/login`.

## Safety Guardrails

The AI Coach uses a controlled pipeline:

1. **Intent analysis:** Classifies user input for urge support, reassurance seeking, crisis/self-harm, education, weekly summary, or out-of-scope content.
2. **Safety routing:** Crisis and reassurance-seeking messages use static safety responses instead of direct generative answers.
3. **Policy enforcement:** Generated responses are validated and redirected if they violate the app's boundaries.
4. **Role visibility:** Regular users receive user-facing coach responses; therapists can see clinical routing metadata for assigned patient review.
5. **Disclaimers:** AI and analytics features are framed as self-reflection tools, not medical advice.
