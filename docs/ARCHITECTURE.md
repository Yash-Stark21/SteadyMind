# SteadyMind Architecture

## Architectural Pattern

SteadyMind uses a layered Spring Boot architecture. Controllers handle HTTP concerns, services own business behavior, repositories isolate persistence, and DTOs define the API contract exposed to browser and REST clients.

### Presentation Layer

- Thymeleaf `@Controller` classes render browser pages such as dashboard, urge logs, exposures, delay attempts, progress, therapist views, and the AI coach.
- `@RestController` classes expose JSON endpoints under `/api/**`.
- Browser pages use a shared JavaScript wrapper, `steady-api.js`, to call same-origin API endpoints with a short-lived bearer token.
- DTOs prevent entities from being exposed directly and reduce over-posting risk.

### Service Layer

- Services enforce ownership checks so users can only access their own logs, tasks, delay attempts, reports, and conversations.
- Admin and therapist services enforce role-specific behavior and patient assignment rules.
- Analytics and reporting services aggregate seven-day progress data for dashboards and downloadable summaries.
- AI services classify user intent, route safety-sensitive messages, validate generated responses, and store conversations.

### Data Access Layer

- Spring Data JPA repositories manage users, urge logs, exposure tasks, delay attempts, AI conversations/messages, and therapist assignments.
- The runtime datasource is MySQL by default and is configured with environment variables.
- The `demo` and `test` profiles use H2 for local review and automated tests.

## Entity Relationships

- `User` has many `UrgeLog` records.
- `User` has many `ExposureTask` records.
- `User` has many `CompulsionDelayAttempt` records.
- `CompulsionDelayAttempt` can optionally reference an `UrgeLog` and/or an `ExposureTask`.
- `User` has many `AiConversation` records, and each conversation has many `AiMessage` records.
- `TherapistAssignment` links a therapist user to an assigned patient user.

## Authentication and Authorization

The application uses two Spring Security filter chains:

- `/api/**` is stateless and requires JWT bearer authentication except for `/api/auth/register` and `/api/auth/login`.
- Browser-rendered pages use normal Spring form login sessions.

Authenticated browser pages call `GET /auth/access-token` to mint a short-lived JWT for same-origin API requests. The token is stored only in JavaScript memory and is attached by `window.steadyApiFetch(...)` to same-origin `/api/**` requests.

Role boundaries:

- `/api/admin/**` requires `ROLE_ADMIN`.
- `/api/therapist/**` requires `ROLE_THERAPIST`.
- `/api/ai/**` allows `ROLE_USER` and `ROLE_THERAPIST`.
- Other protected user APIs require `ROLE_USER`.

Refresh tokens are intentionally not implemented. API clients log in again when the access token expires.

## AI Pipeline and Safety Guardrails

The AI coach is accessed through the `AiClient` interface:

- `mock-ai` uses deterministic keyword-based responses for local demos and tests.
- `openai` uses Spring AI and OpenAI-backed structured output.

The service pipeline first checks for crisis and reassurance-seeking patterns. Those paths bypass direct generative responses and return controlled safety messages. Safe messages can be sent through the AI client, then validated before being returned or stored.

The app is framed as a self-reflection and demo tool, not a medical device, therapy replacement, diagnostic tool, or crisis-support system.

## Progress Analytics and Reports

Progress endpoints aggregate urge logs, exposure tasks, and delay attempts for the authenticated user. The dashboard renders JSON analytics with Chart.js, while downloadable reports are rendered as HTML attachments from Thymeleaf templates.
