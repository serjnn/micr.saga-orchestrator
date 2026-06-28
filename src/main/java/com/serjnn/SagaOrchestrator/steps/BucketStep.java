package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.config.SagaProperties;
import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Order(2)
public class BucketStep extends AbstractSagaStep {

    private final SagaProperties.ServiceProperties serviceProperties;

    public BucketStep(RestClient.Builder restClientBuilder, SagaProperties.ServiceProperties serviceProperties) {
        super(restClientBuilder.build());
        this.serviceProperties = serviceProperties;
    }

    @Override
    public Boolean process(OrderDTO orderDTO) {
        return execute(() -> restClient.delete()
                .uri(serviceProperties.bucket().clearUrl(), orderDTO.clientID()), "bucket process");
    }

    @Override
    public Boolean revert(OrderDTO orderDTO) {
        return execute(() -> restClient.post()
                .uri(serviceProperties.bucket().restoreUrl())
                .body(orderDTO), "bucket revert");
    }
}
