package com.serjnn.SagaOrchestrator.dto;

public sealed interface SagaStepResult permits SagaStepResult.Success, SagaStepResult.Failure {

    record Success() implements SagaStepResult {}

    record Failure(String message, Throwable cause) implements SagaStepResult {
        public Failure(String message) {
            this(message, null);
        }
    }
}
