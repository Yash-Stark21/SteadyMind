# SteadyMind Interview Notes and Talking Points

This document contains concise talking points for technical interviews and recruiter conversations.

## 2-Minute Project Explanation

"SteadyMind is a self-guided Spring Boot web application for tracking urges, practicing compulsion delay, planning exposure tasks, reviewing progress, and using a guarded AI wellness coach. I built it as a portfolio project around a sensitive domain, so the architecture emphasizes safety boundaries, user scoping, JWT-secured APIs, and clear demo setup. The app includes Thymeleaf pages, REST APIs, role-based admin and therapist views, progress analytics, downloadable reports, Postman coverage, Swagger UI, and automated tests."

## Architecture Explanation

"The app follows a layered architecture. Controllers handle Thymeleaf pages and REST APIs. Services own business rules, analytics, report generation, user scoping, and AI safety orchestration. Repositories use Spring Data JPA. DTOs define the API boundary so entities are not exposed directly."

## Security Explanation

"Security uses two filter chains. `/api/**` is stateless and secured with JWT bearer tokens, while browser-rendered pages use Spring form login sessions. Browser pages call `/auth/access-token` to mint a short-lived JWT for same-origin API calls. Role rules separate user, admin, and therapist capabilities, and service-level ownership checks prevent cross-account access."

## AI Integration Explanation

"The AI coach is behind an `AiClient` abstraction. The `mock-ai` profile gives deterministic local responses, and the `openai` profile uses Spring AI/OpenAI structured output. Before returning a response, the system checks for crisis and reassurance-seeking patterns, routes risky cases to static safety messages, and validates model output. The app is explicitly framed as a reflection tool, not therapy or diagnosis."

## Database and Demo Setup

"The normal runtime configuration is MySQL through environment variables. For recruiters and local demos, the `demo,mock-ai` profile uses H2 and deterministic AI responses, so the project can run without MySQL or an OpenAI key. Tests also use H2."

## Testing Explanation

"The suite currently has 53 passing tests. It includes service-level unit tests for AI safety, policy routing, authentication, analytics, therapist behavior, and weekly summaries, plus MockMvc integration tests for JWT login, role boundaries, stateless API behavior, browser session token minting, and logout."

## Strong Interview Answers

**Why Spring Boot?**
Spring Boot provides a strong ecosystem for REST APIs, server-rendered views, JPA, validation, and security. It let me build a full-stack Java application while using production-style patterns.

**How do you prevent users from reading each other's data?**
API authentication identifies the current user, and services always query or mutate resources through that user's ownership boundary. Therapist access is also assignment-based rather than globally open.

**Why use DTOs?**
DTOs keep the API contract separate from entity mappings, reduce accidental field exposure, and prevent mass-assignment problems such as users trying to set privileged fields.

**How does the browser call stateless APIs if pages use session login?**
After form login, browser pages call `/auth/access-token`. That endpoint is protected by the web session and returns a short-lived JWT. A shared JS wrapper attaches it only to same-origin `/api/**` calls.

**What happens if OpenAI fails?**
Provider exceptions and invalid responses fall back to controlled safe messages. The rest of the app remains usable, and the mock profile can run without network access.

**How do you handle database migrations?**
For this portfolio version, Hibernate `ddl-auto` is environment-controlled. For production, I would add Flyway or Liquibase and replace schema auto-update with versioned migrations.

**What would you improve next?**
I would add controller validation tests, `@DataJpaTest` repository coverage, JaCoCo reporting, Flyway migrations, and a small hosted demo environment.

## Most Important Takeaway

This project shows end-to-end Java/Spring delivery: domain modeling, security, REST APIs, server-rendered UI, AI integration with guardrails, analytics, documentation, and automated tests.
