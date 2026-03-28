package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Order(2)
public class BucketStep implements SagaStep {

    private static final Logger log = LoggerFactory.getLogger(BucketStep.class);

    private final RestClient restClient;

    @Value("${services.bucket.clear-url}")
    private String clearUrl;

    @Value("${services.bucket.restore-url}")
    private String restoreUrl;

    public BucketStep(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public Boolean process(OrderDTO orderDTO) {
        log.info("bucket process");
        try {
            var response = restClient.post()
                    .uri(clearUrl)
                    .body(orderDTO.clientID())
                    .retrieve()
                    .toBodilessEntity();


            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                log.info("Bucket failed with status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Bucket service is unavailable, triggering rollback: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean revert(OrderDTO orderDTO) {
        log.info("bucket revert");
        try {
            return restClient.post()
                    .uri(restoreUrl)
                    .body(orderDTO)
                    .retrieve()
                    .body(Boolean.class);
        } catch (Exception e) {
            log.error("Bucket service is unavailable, triggering rollback: {}", e.getMessage());
            return false;
        }
    }
}
