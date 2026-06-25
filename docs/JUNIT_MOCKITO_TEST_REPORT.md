# JUnit and Mockito Test Report

Generated on: 2026-06-25

## Executive Summary

SteadyMind uses JUnit Jupiter, Spring Boot Test, Mockito, MockMvc, Spring Security Test, and H2. The current suite contains 10 test classes and 53 test methods. A full Maven test run passed with 53 tests, 0 failures, 0 errors, and 0 skipped tests.

The strongest coverage is around AI safety behavior, AI fallback handling, JWT/API security rules, authentication, therapist access, weekly summaries, and progress analytics. The main remaining gap is broad controller and repository coverage for every CRUD workflow.

## Test Stack

- Java 25
- Spring Boot Test through `spring-boot-starter-test`
- JUnit Jupiter 6
- Mockito 5
- AssertJ
- Spring Security Test
- MockMvc for security integration checks
- H2 for test persistence

The test profile uses `src/test/resources/application-test.properties` with an H2 in-memory database and Spring AI model integrations disabled. The `mock-ai` profile supplies deterministic AI responses where a Spring context needs an AI client.

No JaCoCo or other coverage plugin is configured yet.

## Test Inventory

| Test class | Tests | Main purpose |
| --- | ---: | --- |
| `SteadyaiApplicationTests` | 1 | Spring context smoke test with `test` and `mock-ai` profiles |
| `OpenAiCoachClientTest` | 3 | OpenAI client validation, fallback, and provider-exception behavior |
| `ApiSecurityIntegrationTest` | 12 | JWT login, API role rules, stateless API behavior, web session bridge, logout |
| `AiCoachServiceTest` | 11 | AI coach routing, fallback, metadata, and repository save behavior |
| `AiPolicyServiceTest` | 3 | Policy overrides for reassurance, crisis, and out-of-scope responses |
| `AiSafetyServiceTest` | 5 | Keyword-based reassurance/crisis detection and null/blank safety |
| `AuthServiceTest` | 2 | User registration password encoding, role assignment, duplicate email rejection |
| `ProgressAnalyticsServiceTest` | 6 | Empty analytics, averages, trend, trigger breakdown, exposure and delay metrics |
| `TherapistServiceTest` | 7 | Therapist assignment and assigned-patient access behavior |
| `WeeklySummaryServiceTest` | 3 | Empty summary, populated summary, unsafe AI summary fallback |

Total: 53 tests.

## Current Passing Test Run

Command:

```powershell
.\mvnw.cmd test
```

Result:

```text
Tests run: 53, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The successful run emits a Mockito/JDK warning about inline mocking self-attaching a Java agent. This does not fail the build, but future JDK versions may require configuring Mockito's agent explicitly or reducing static mocking.

## What Is Well Covered

- AI safety classification for reassurance-seeking, crisis, null, and blank input.
- AI policy routing that turns unsafe or out-of-scope responses into controlled static messages.
- AI coach fallback behavior when the provider fails or returns invalid output.
- JWT login, expired-token rejection, stateless API requests, role-specific access, and browser access-token minting from a web session.
- Auth registration behavior, including BCrypt encoding and duplicate-email rejection.
- Therapist assignment and assigned-patient access rules.
- Weekly summary aggregation and fallback behavior.
- Progress analytics calculations for trends, trigger breakdowns, intensity buckets, exposure counts, and delay metrics.

## Main Test Gaps

- Most REST and MVC controllers still lack direct endpoint tests beyond security-focused integration coverage.
- Repository behavior is not covered with `@DataJpaTest`; derived queries and mappings are mostly exercised indirectly.
- DTO validation and `GlobalExceptionHandler` behavior need dedicated MockMvc coverage.
- Coverage is not measured, so the project cannot report line, branch, method, or package coverage.

## Recommendations

1. Add MockMvc tests for urge logs, exposure tasks, delay attempts, reports, admin, therapist, and AI endpoints.
2. Add `@DataJpaTest` coverage for repository query methods and entity relationships.
3. Add JaCoCo with an initial reporting-only configuration before enforcing a gate.
4. Configure Mockito's Java agent explicitly for Java 25+ compatibility, or remove static mocking where practical.

## Overall Assessment

The suite is credible for a portfolio project: it passes, covers meaningful safety and security paths, and demonstrates unit plus integration testing. The next improvement is breadth, especially controller validation and repository tests.
