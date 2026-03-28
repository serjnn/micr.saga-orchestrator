package com.serjnn.SagaOrchestrator.config;

import java.util.function.Predicate;

public class RetryResultPredicate implements Predicate<Boolean> {
    @Override
    public boolean test(Boolean result) {
        return result != null && !result;
    }
}
