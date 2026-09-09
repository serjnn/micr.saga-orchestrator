package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.config.SagaProperties;
import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.dto.SagaStepResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ClientBalanceStep extends AbstractSagaStep {

    private final SagaProperties.ServiceProperties serviceProperties;

    public ClientBalanceStep(RestClient.Builder restClientBuilder, SagaProperties.ServiceProperties serviceProperties) {
        super(restClientBuilder.build());
        this.serviceProperties = serviceProperties;
    }

    @Override
    public SagaStepResult process(OrderDTO orderDTO) {
        return execute(() -> restClient.post()
                .uri(serviceProperties.client().deductUrl())
                .body(orderDTO), "client process");
    }

    @Override
    public SagaStepResult revert(OrderDTO orderDTO) {
        return execute(() -> restClient.post()
                .uri(serviceProperties.client().restoreUrl())
                .body(orderDTO), "client revert");
    }
}
