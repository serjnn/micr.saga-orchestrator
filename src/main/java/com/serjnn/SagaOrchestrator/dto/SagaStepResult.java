package com.serjnn.SagaOrchestrator.dto;

public sealed interface SagaStepResult permits SagaStepResult.Success, SagaStepResult.Failure {

    record Success() implements SagaStepResult {}

    record Failure(String message, Throwable cause, boolean retryable) implements SagaStepResult {
        public Failure(String message) {
            this(message, null, true);
        }

        public Failure(String message, Throwable cause) {
            this(message, cause, true);
        }

        public Failure(String message, boolean retryable) {
            this(message, null, retryable);
        }
    }
}
