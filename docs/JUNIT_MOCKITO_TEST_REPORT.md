# JUnit and Mockito Test Report

Generated on: 2026-05-29

## Executive Summary

SteadyAI uses JUnit Jupiter through Spring Boot's test starter and Mockito for service-level unit tests. The current suite contains 7 test classes and 25 test methods. A full Maven test run passed with 25 tests, 0 failures, 0 errors, and 0 skipped tests.

The testing strategy is currently concentrated on service-layer logic, especially AI safety/policy behavior, AI coach fallback handling, authentication registration, weekly summaries, and progress analytics. There is one Spring Boot context smoke test. Controllers, repositories, security behavior, entity validation, and several services are not directly tested.

## Test Stack

The project is a Maven Spring Boot application configured with:

- Java 25 in `pom.xml`.
- Spring Boot parent `4.0.6`.
- `spring-boot-starter-test` for JUnit Jupiter, AssertJ, Mockito, Spring Test, and the JUnit Platform.
- `spring-security-test` for security testing support, although it is not currently used in any test class.
- H2 as a test-scoped in-memory database.
- `src/test/resources/application-test.properties` configures an H2 database, `create-drop` schema generation, and disables Spring AI model/vector-store integrations for the `test` profile.

Resolved test dependencies from `mvn dependency:tree`:

- JUnit Jupiter `6.0.3`
- Mockito Core `5.20.0`
- Mockito JUnit Jupiter `5.20.0`
- AssertJ Core `3.27.7`

No JaCoCo or other coverage plugin is configured in the Maven build.

## Test Inventory

| Test class | Tests | Main purpose |
| --- | ---: | --- |
| `SteadyaiApplicationTests` | 1 | Spring context smoke test with `test` and `mock-ai` profiles |
| `AiCoachServiceTest` | 5 | AI coach fallback, crisis/reassurance pre-checks, repository save behavior |
| `AiPolicyServiceTest` | 3 | Policy overrides for reassurance, crisis, and out-of-scope responses |
| `AiSafetyServiceTest` | 5 | Keyword-based reassurance/crisis detection and null/blank safety |
| `AuthServiceTest` | 2 | User registration password encoding, role assignment, duplicate email rejection |
| `ProgressAnalyticsServiceTest` | 6 | Empty analytics, averages, trigger breakdown, intensity distribution, trend, exposure/delay metrics |
| `WeeklySummaryServiceTest` | 3 | Empty summary, populated summary, unsafe AI summary fallback |

Total: 25 tests.

## How JUnit Is Used

The suite uses JUnit Jupiter directly:

- `@Test` is used across all test classes.
- `@BeforeEach` is used for object setup in service tests.
- `@AfterEach` is used where static Mockito mocks must be closed.
- `@ExtendWith(MockitoExtension.class)` is used in `WeeklySummaryServiceTest` and `ProgressAnalyticsServiceTest`.
- `@SpringBootTest` plus `@ActiveProfiles({"test", "mock-ai"})` is used in `SteadyaiApplicationTests` to verify the full Spring application context can start.

Assertion style is mixed:

- JUnit assertions are used heavily in `AuthServiceTest`, `ProgressAnalyticsServiceTest`, and `WeeklySummaryServiceTest`.
- AssertJ fluent assertions are used in `AiCoachServiceTest`, `AiPolicyServiceTest`, and `AiSafetyServiceTest`.

There are no parameterized tests, nested tests, dynamic tests, test tags, or explicit test ordering.

## How Mockito Is Used

Mockito is used to isolate service logic from repositories, AI clients, validators, and policy/safety collaborators.

Main Mockito patterns:

- Annotation-based mocks with `@Mock` in `AiCoachServiceTest`, `WeeklySummaryServiceTest`, and `ProgressAnalyticsServiceTest`.
- `@InjectMocks` in `AiCoachServiceTest` to build `AiCoachServiceImpl` from mocked collaborators.
- Manual mocks with `mock(...)` in `AuthServiceTest`.
- `MockitoExtension` in two test classes.
- Manual `MockitoAnnotations.openMocks(this)` in `AiCoachServiceTest`.
- Static mocking with `mockStatic(SecurityUtils.class)` in `WeeklySummaryServiceTest` and `ProgressAnalyticsServiceTest`.
- Stubbing with `when(...).thenReturn(...)`, `thenThrow(...)`, `doThrow(...)`, and `thenAnswer(...)`.
- Behavior verification with `verify(...)`, including `never()`.
- `ArgumentCaptor` to inspect saved `User` and `AiMessage` entities.
- `lenient()` stubbing for `UserRepository.findByEmail(...)` in tests that mock `SecurityUtils.getCurrentUser()`.

The most advanced Mockito usage is static mocking of `SecurityUtils.getCurrentUser()`. This keeps service tests isolated, but it requires Mockito inline mocking support and causes the current JDK warning about dynamic Java agent loading.

## Current Passing Test Run

Command run:

```powershell
.\mvnw.cmd test
```

Result:

```text
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The sandboxed run initially failed because Maven could not write compiled classes under `target/classes`. Running Maven outside the sandbox completed successfully.

The successful test run emitted a Mockito/JDK warning:

```text
Mockito is currently self-attaching to enable the inline-mock-maker.
This will no longer work in future releases of the JDK.
```

This is caused by inline/static mocking and Java's tightening of dynamic agent loading behavior.

## What Is Well Covered

- AI safety classification for obvious reassurance-seeking, crisis, null, and blank input.
- AI policy guardrails that convert unsafe or out-of-scope responses into controlled static messages.
- AI coach fallback behavior when the AI client fails or returns invalid output.
- AI coach bypass behavior for crisis and reassurance pre-checks.
- Repository persistence intent for AI messages and new registered users.
- Weekly summary aggregation and fallback text when AI-generated text is unsafe.
- Progress analytics calculations for totals, averages, trends, trigger breakdowns, intensity buckets, exposure counts, and delay metrics.
- Basic Spring context startup with H2 and mock AI profile.

## Main Test Gaps

Controllers are not directly tested. There are REST and MVC controllers for auth, urge logs, exposure tasks, delay attempts, AI coach, analytics, reports, admin, therapist, and views, but no `@WebMvcTest`, `MockMvc`, or integration endpoint tests.

Several services have no direct tests:

- `UrgeLogService`
- `ExposureTaskServiceImpl`
- `CompulsionDelayAttemptServiceImpl`
- `ProgressReportService`
- `AdminService`
- `TherapistService`
- `CustomUserDetailsService`
- `SecurityUtils`
- `AiResponseValidator`
- `MockAiClient`
- `SpringAiClient`

Repository behavior is not tested with `@DataJpaTest`. Current repository calls are mocked, so derived query correctness, entity mappings, relationships, and H2 schema behavior are only indirectly exercised by the context test.

Security behavior is not tested despite `spring-security-test` being present. There are no tests for route access rules, login requirements, role restrictions, CSRF behavior, or authenticated-user mapping.

Validation behavior is not directly tested. DTO validation annotations and `GlobalExceptionHandler` behavior are not covered by controller tests.

There is no coverage measurement, so the project cannot quantify line, branch, method, or package coverage.

## Risks and Observations

Static mocking of `SecurityUtils` is a maintainability risk. It couples service tests to static authentication access and triggers Mockito inline agent warnings on Java 25. A cleaner long-term design would inject a current-user provider or authentication facade.

The test suite mixes AssertJ and JUnit assertion styles. This is functional, but standardizing on one style would make test failures and test code more consistent.

`AiCoachServiceTest` uses `MockitoAnnotations.openMocks(this)` but does not close the returned `AutoCloseable`. Replacing it with `@ExtendWith(MockitoExtension.class)` would match the other Mockito tests and avoid resource lifecycle issues.

The Spring context test starts a relatively broad application context and performs schema creation through H2. It confirms bootstrapping, but it does not assert endpoint behavior or data access behavior.

The current tests are deterministic enough for service logic, but several analytics tests use `LocalDate.now()` and `LocalDateTime.now()`. These are acceptable at this scale but could become brittle around time-zone/date-boundary cases. Injecting a `Clock` would make date logic easier to test.

## Recommendations

1. Add controller tests with `@WebMvcTest` and `MockMvc` for core REST endpoints:
   - auth registration
   - urge logs
   - exposure tasks
   - delay attempts
   - AI coach
   - analytics/report endpoints

2. Add focused service tests for currently untested business services:
   - `UrgeLogService`
   - `ExposureTaskServiceImpl`
   - `CompulsionDelayAttemptServiceImpl`
   - `ProgressReportService`
   - `AdminService`
   - `TherapistService`

3. Add `@DataJpaTest` coverage for repository query methods and entity relationships.

4. Use `spring-security-test` to cover authentication and authorization rules, especially admin/therapist/user role boundaries.

5. Replace static `SecurityUtils` mocking with an injectable current-user abstraction where practical.

6. Convert `AiCoachServiceTest` to `@ExtendWith(MockitoExtension.class)` instead of manual `MockitoAnnotations.openMocks(this)`.

7. Configure Mockito's Java agent explicitly for Java 25 and future JDK compatibility, or remove inline/static mocking.

8. Add JaCoCo or another coverage plugin to Maven and set a modest initial coverage gate after the missing service/controller tests are added.

## Overall Assessment

The current JUnit and Mockito usage is practical and mostly idiomatic for service-level unit testing. The suite verifies several important safety and analytics paths and currently passes. The main weakness is breadth: most web, security, data access, and several business workflows are not yet covered. The highest-value next step is to add MockMvc controller/security tests and direct unit tests for the untested CRUD-style services.
