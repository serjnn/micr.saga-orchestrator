package com.serjnn.SagaOrchestrator.services;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.steps.SagaStep;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Service
public class OrchService {

    private static final Logger log = LoggerFactory.getLogger(OrchService.class);

    private final List<SagaStep> steps;
    private final RetryRegistry retryRegistry;

    public OrchService(List<SagaStep> steps, RetryRegistry retryRegistry) {
        this.steps = steps;
        this.retryRegistry = retryRegistry;
    }

    public boolean start(OrderDTO orderDTO) {
        log.info("starting transaction");
        List<SagaStep> completedSteps = new ArrayList<>();

        try {
            for (SagaStep step : steps) {
                boolean success = step.process(orderDTO);
                if (success) {
                    completedSteps.add(step);
                } else {
                    revert(orderDTO, completedSteps);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error in process: {}, triggering rollback.", e.getMessage());
            revert(orderDTO, completedSteps);
            return false;
        }
    }

    private void revert(OrderDTO orderDTO, List<SagaStep> completedSteps) {
        log.info("Rolling back completed steps: {}", completedSteps.size());
        List<SagaStep> reverseSteps = new ArrayList<>(completedSteps);
        Collections.reverse(reverseSteps);

        for (SagaStep step : reverseSteps) {
            String retryName = step.getClass().getSimpleName() + "RevertRetry";
            Retry retry = retryRegistry.retry(retryName);

            Supplier<Boolean> revertSupplier = Retry.decorateSupplier(retry, () -> {
                log.info("Attempting revert for step: {}", step.getClass().getSimpleName());
                return step.revert(orderDTO);
            });

            try {
                Boolean result = revertSupplier.get();
                if (Boolean.FALSE.equals(result)) {
                    log.error("Critical: Failed to revert step: {} after retries.", step.getClass().getSimpleName());
                }
            } catch (Exception e) {
                log.error("Critical: Exception during revert for step: {} after retries. Error: {}", 
                        step.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
