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
import java.util.function.Function;
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
        log.info("Starting saga: {}", orderCreationSaga.name());
        List<SagaStep> completedSteps = new ArrayList<>();

        try {
            for (SagaStep step : orderCreationSaga.steps()) {
                SagaStepResult result = executeStep(step, "Process", step::process, orderDTO, retryProperties.processEnabled());

                if (result instanceof SagaStepResult.Success) {
                    completedSteps.add(step);
                } else {
                    if (result instanceof SagaStepResult.Failure failure) {
                        log.error("Step {} failed: {}", step.getName(), failure.message());
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
            SagaStepResult result = executeStep(step, "Revert", step::revert, orderDTO, retryProperties.revertEnabled());

            if (result instanceof SagaStepResult.Failure failure) {
                log.error("Critical: Failed to revert step: {} after retries/attempts. Error: {}", 
                        step.getName(), failure.message());
            }
        }
    }

    private SagaStepResult executeStep(
            SagaStep step,
            String phase,
            Function<OrderDTO, SagaStepResult> action,
            OrderDTO orderDTO,
            boolean retryEnabled
    ) {
        String stepName = step.getName();
        if (retryEnabled) {
            String retryName = stepName + ("Process".equalsIgnoreCase(phase) ? "Process" : "") + retryProperties.suffix();
            Retry retry = retryRegistry.retry(retryName);
            Supplier<SagaStepResult> supplier = Retry.decorateSupplier(retry, () -> {
                log.info("Attempting {} for step: {}", phase.toLowerCase(), stepName);
                return action.apply(orderDTO);
            });
            try {
                return supplier.get();
            } catch (Exception e) {
                return new SagaStepResult.Failure(e.getMessage(), e, false);
            }
        } else {
            try {
                return action.apply(orderDTO);
            } catch (Exception e) {
                return new SagaStepResult.Failure(e.getMessage(), e, false);
            }
        }
    }
}
