package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Order(1)
public class ClientBalanceStep implements SagaStep {

    private static final Logger log = LoggerFactory.getLogger(ClientBalanceStep.class);

    private final RestClient restClient;

    @Value("${services.client.deduct-url}")
    private String deductUrl;

    @Value("${services.client.restore-url}")
    private String restoreUrl;

    public ClientBalanceStep(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public Boolean process(OrderDTO orderDTO) {
        log.info("client process");
        try {
            var response = restClient.post()
                    .uri(deductUrl)
                    .body(orderDTO)
                    .retrieve()
                    .toBodilessEntity();


            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                log.info("client failed with status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Client service is unavailable, triggering rollback: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean revert(OrderDTO orderDTO) {
        log.info("client revert");
        try {
            var response = restClient.post()
                    .uri(restoreUrl)
                    .body(orderDTO)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                log.info("client revert failed with status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Client service is unavailable, triggering rollback: {}", e.getMessage());
            return false;
        }
    }
}
