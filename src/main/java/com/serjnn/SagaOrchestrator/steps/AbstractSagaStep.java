package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
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

    protected Boolean execute(Supplier<RestClient.RequestHeadersSpec<?>> requestSpecSupplier, String operationName) {
        log.info("Executing step operation: {}", operationName);
        try {
            ResponseEntity<Void> response = requestSpecSupplier.get().retrieve().toBodilessEntity();
            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                log.info("{} failed with status: {}", operationName, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("{} is unavailable, triggering rollback: {}", operationName, e.getMessage());
            return false;
        }
    }
}
