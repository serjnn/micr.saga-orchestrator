# Saga Orchestrator Refactoring Plan

## Overview
Refactor the `SagaOrchestrator` microservice to resolve architectural issues, remove code duplication, fix integration tests, and introduce type-safe configuration, custom result models, configurable retries, and explicit saga definitions.

## Context
- Files involved:
  - [SagaStep.java](file:///C:/Users/sersh/IdeaProjects/micr.saga-orchestrator/src/main/java/com/serjnn/SagaOrchestrator/steps/SagaStep.java)
  - [ClientBalanceStep.java](file:///C:/Users/sersh/IdeaProjects/micr.saga-orchestrator/src/main/java/com/serjnn/SagaOrchestrator/steps/ClientBalanceStep.java)
  - [BucketStep.java](file:///C:/Users/sersh/IdeaProjects/micr.saga-orchestrator/src/main/java/com/serjnn/SagaOrchestrator/steps/BucketStep.java)
  - [OrderStep.java](file:///C:/Users/sersh/IdeaProjects/micr.saga-orchestrator/src/main/java/com/serjnn/SagaOrchestrator/steps/OrderStep.java)
  - [OrchService.java](file:///C:/Users/sersh/IdeaProjects/micr.saga-orchestrator/src/main/java/com/serjnn/SagaOrchestrator/services/OrchService.java)
  - [OrchController.java](file:///C:/Users/sersh/IdeaProjects/micr.saga-orchestrator/src/main/java/com/serjnn/SagaOrchestrator/controller/OrchController.java)
  - [SagaOrchestratorIntegrationTest.java](file:///C:/Users/sersh/IdeaProjects/micr.saga-orchestrator/src/test/java/com/serjnn/SagaOrchestrator/SagaOrchestratorIntegrationTest.java)

## Development Approach
- **testing approach**: Regular (code first, then tests)
- Run `mvn test` after each change to verify correctness.
- Complete each task fully before moving to the next.

## Progress Tracking
- [x] Task 1: Fix integration tests (WireMock & HTTP methods mismatch)
- [x] Task 2: Centralize configuration properties using type-safe `@ConfigurationProperties`
- [x] Task 3: Extract a common parent class for REST steps to eliminate code duplication
- [ ] Task 4: Introduce custom sealed `SagaStepResult` / `SagaResult` model instead of primitive `Boolean`
- [ ] Task 5: Implement explicit saga definitions (`SagaDefinition`) instead of Spring `@Order` collection injection
- [ ] Task 6: Add configurable retry resilience to the process phase

---

## Technical Details

### 1. Fix Integration Tests
Currently, `SagaOrchestratorIntegrationTest` fails due to:
- Mismatched HTTP methods (`DELETE` vs `POST` in stubs).
- Placeholder mismatches (URLs in tests lack path parameters like `{clientId}` or `{orderId}`).

### 2. Type-Safe Configuration Properties
Create a `SagaProperties` config class mapping both service URLs and retry configurations.
```java
@ConfigurationProperties(prefix = "services")
public record ServiceProperties(
    BucketProperties bucket,
    ClientProperties client,
    OrderProperties order
) {
    public record BucketProperties(String clearUrl, String restoreUrl) {}
    public record ClientProperties(String deductUrl, String restoreUrl) {}
    public record OrderProperties(String createUrl, String removeUrl) {}
}
```

### 3. Base Step Class
Create `AbstractSagaStep` containing the shared `RestClient` logic to execute operations cleanly and safely without boilerplate catch blocks.

### 4. Sealed Result Types (Java 17)
Use Java 17 features:
```java
public sealed interface SagaStepResult permits SagaStepResult.Success, SagaStepResult.Failure {
    record Success() implements SagaStepResult {}
    record Failure(String message, Throwable cause) implements SagaStepResult {}
}
```

### 5. Explicit Saga Definitions
Introduce a `SagaDefinition` holder class:
```java
public record SagaDefinition(String name, List<SagaStep> steps) {}
```
This avoids blind list injection in `OrchService`.

---

## Implementation Steps

### Task 1: Fix Integration Tests (WireMock & HTTP methods mismatch)
**Files:**
- Modify: `src/test/java/com/serjnn/SagaOrchestrator/SagaOrchestratorIntegrationTest.java`

- [x] Update wiremock stubs to match exact HTTP methods (e.g. `delete` for bucket clear and order remove).
- [x] Update property values in `@TestPropertySource` to include placeholders (e.g. `/api/v1/clear/{clientId}`).
- [x] Run `mvn test` to verify integration tests pass successfully.

### Task 2: Type-safe Configuration Properties
**Files:**
- Create: `src/main/java/com/serjnn/SagaOrchestrator/config/SagaProperties.java`
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/SagaOrchestratorApplication.java`

- [x] Create `@ConfigurationProperties` record/class representing URLs and resilience settings.
- [x] Enable configuration properties in `SagaOrchestratorApplication`.
- [x] Run tests to ensure everything compiles and boots correctly.

### Task 3: Base Step Class
**Files:**
- Create: `src/main/java/com/serjnn/SagaOrchestrator/steps/AbstractSagaStep.java`
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/steps/ClientBalanceStep.java`
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/steps/BucketStep.java`
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/steps/OrderStep.java`

- [x] Implement `AbstractSagaStep` wrapping common `RestClient` execute pattern.
- [x] Refactor the three steps to extend `AbstractSagaStep` and simplify their methods.
- [x] Run tests to verify logic is preserved.

### Task 4: Sealed Result Types
**Files:**
- Create: `src/main/java/com/serjnn/SagaOrchestrator/dto/SagaStepResult.java`
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/steps/SagaStep.java`
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/steps/AbstractSagaStep.java`
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/services/OrchService.java`

- [ ] Create sealed interface `SagaStepResult`.
- [ ] Update `SagaStep` interface and implementation to return `SagaStepResult`.
- [ ] Update `OrchService` process/revert logic to match the new result type.
- [ ] Run tests to ensure correct integration.

### Task 5: Explicit Saga Definitions
**Files:**
- Create: `src/main/java/com/serjnn/SagaOrchestrator/config/SagaDefinition.java`
- Create: `src/main/java/com/serjnn/SagaOrchestrator/config/SagaConfig.java`
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/services/OrchService.java`

- [ ] Create `SagaDefinition` bean class.
- [ ] Create `SagaConfig` declaring the bean with specific step instances in order.
- [ ] Modify `OrchService` to run a given `SagaDefinition` instead of auto-wired `@Order` list.
- [ ] Run tests to verify the orchestrator executes the saga in the correct order.

### Task 6: Configurable Process Phase Retries
**Files:**
- Modify: `src/main/java/com/serjnn/SagaOrchestrator/services/OrchService.java`

- [ ] Decorate the step `process` call with Retry from `RetryRegistry` if process retry is enabled.
- [ ] Add config properties to enable/disable process/revert retries.
- [ ] Run tests to verify all steps function as expected.
