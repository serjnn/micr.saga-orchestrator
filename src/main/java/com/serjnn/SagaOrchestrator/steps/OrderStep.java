package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Order(3)
public class OrderStep implements SagaStep {

    private static final Logger log = LoggerFactory.getLogger(OrderStep.class);

    private final RestClient restClient;

    public OrderStep(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public Boolean process(OrderDTO orderDTO) {
        log.info("order process");
        try {
            var response = restClient.post()
                    .uri("http://order/api/v1/create")
                    .body(orderDTO)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                log.info("order failed with status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("order service is unavailable, triggering rollback: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean revert(OrderDTO orderDTO) {
        log.info("order revert");
        try {
            return restClient.post()
                    .uri("http://order/api/v1/remove")
                    .body(orderDTO.orderId())
                    .retrieve()
                    .body(Boolean.class);
        } catch (Exception e) {
            log.error("Error during order revert: {}", e.getMessage());
            return false;
        }
    }
}
