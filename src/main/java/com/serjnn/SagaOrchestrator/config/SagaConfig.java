package com.serjnn.SagaOrchestrator.config;

import com.serjnn.SagaOrchestrator.steps.BucketStep;
import com.serjnn.SagaOrchestrator.steps.ClientBalanceStep;
import com.serjnn.SagaOrchestrator.steps.OrderStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SagaConfig {

    @Bean
    public SagaDefinition orderCreationSaga(
            ClientBalanceStep clientBalanceStep,
            BucketStep bucketStep,
            OrderStep orderStep
    ) {
        return new SagaDefinition("Order Creation Saga", List.of(clientBalanceStep, bucketStep, orderStep));
    }
}
