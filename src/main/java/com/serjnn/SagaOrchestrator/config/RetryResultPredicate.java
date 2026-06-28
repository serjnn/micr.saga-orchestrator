package com.serjnn.SagaOrchestrator.config;

import com.serjnn.SagaOrchestrator.dto.SagaStepResult;
import java.util.function.Predicate;

public class RetryResultPredicate implements Predicate<SagaStepResult> {
    @Override
    public boolean test(SagaStepResult result) {
        return result instanceof SagaStepResult.Failure;
    }
}
