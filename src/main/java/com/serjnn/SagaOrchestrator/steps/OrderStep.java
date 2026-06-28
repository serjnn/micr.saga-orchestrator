package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.config.SagaProperties;
import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Order(3)
public class OrderStep extends AbstractSagaStep {

    private final SagaProperties.ServiceProperties serviceProperties;

    public OrderStep(RestClient.Builder restClientBuilder, SagaProperties.ServiceProperties serviceProperties) {
        super(restClientBuilder.build());
        this.serviceProperties = serviceProperties;
    }

    @Override
    public Boolean process(OrderDTO orderDTO) {
        return execute(() -> restClient.post()
                .uri(serviceProperties.order().createUrl())
                .body(orderDTO), "order process");
    }

    @Override
    public Boolean revert(OrderDTO orderDTO) {
        return execute(() -> restClient.delete()
                .uri(serviceProperties.order().removeUrl(), orderDTO.orderId()), "Order revert");
    }
}
