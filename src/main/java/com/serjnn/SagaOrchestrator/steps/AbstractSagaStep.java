package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.dto.SagaStepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import java.util.function.Supplier;

public abstract class AbstractSagaStep implements SagaStep {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final RestClient restClient;

    protected AbstractSagaStep(RestClient restClient) {
        this.restClient = restClient;
    }

    protected SagaStepResult execute(Supplier<RestClient.RequestHeadersSpec<?>> requestSpecSupplier, String operationName) {
        log.info("Executing step operation: {}", operationName);
        try {
            ResponseEntity<Void> response = requestSpecSupplier.get().retrieve().toBodilessEntity();
            if (response.getStatusCode().is2xxSuccessful()) {
                return new SagaStepResult.Success();
            } else {
                String errorMsg = String.format("%s failed with status: %s", operationName, response.getStatusCode());
                log.warn(errorMsg);
                return new SagaStepResult.Failure(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = String.format("%s is unavailable, triggering rollback: %s", operationName, e.getMessage());
            log.error(errorMsg);
            return new SagaStepResult.Failure(errorMsg, e);
        }
    }
}
