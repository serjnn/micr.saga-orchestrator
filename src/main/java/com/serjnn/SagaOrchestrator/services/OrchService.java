package com.serjnn.SagaOrchestrator.services;

import com.serjnn.SagaOrchestrator.config.SagaDefinition;
import com.serjnn.SagaOrchestrator.config.SagaProperties;
import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.dto.SagaStepResult;
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
    private final RetryRegistry retryRegistry;
    private final SagaProperties.RetryProperties retryProperties;
    private final SagaDefinition orderCreationSaga;

    public OrchService(SagaDefinition orderCreationSaga, RetryRegistry retryRegistry, SagaProperties.RetryProperties retryProperties) {
        this.orderCreationSaga = orderCreationSaga;
        this.retryRegistry = retryRegistry;
        this.retryProperties = retryProperties;
    }

    public boolean start(OrderDTO orderDTO) {
        log.info("starting saga: {}", orderCreationSaga.name());
        List<SagaStep> completedSteps = new ArrayList<>();

        try {
            for (SagaStep step : orderCreationSaga.steps()) {
                SagaStepResult result = step.process(orderDTO);
                if (result instanceof SagaStepResult.Success) {
                    completedSteps.add(step);
                } else {
                    if (result instanceof SagaStepResult.Failure failure) {
                        log.error("Step {} failed: {}", step.getClass().getSimpleName(), failure.message());
                    }
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
            String retryName = step.getClass().getSimpleName() + retryProperties.suffix();

            Retry retry = retryRegistry.retry(retryName);

            Supplier<SagaStepResult> revertSupplier = Retry.decorateSupplier(retry, () -> {
                log.info("Attempting revert for step: {}", step.getClass().getSimpleName());
                return step.revert(orderDTO);
            });

            try {
                SagaStepResult result = revertSupplier.get();
                if (result instanceof SagaStepResult.Failure failure) {
                    log.error("Critical: Failed to revert step: {} after retries. Error: {}", 
                            step.getClass().getSimpleName(), failure.message());
                }
            } catch (Exception e) {
                log.error("Critical: Exception during revert for step: {} after retries. Error: {}", 
                        step.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
