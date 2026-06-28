package com.serjnn.SagaOrchestrator.config;

import com.serjnn.SagaOrchestrator.dto.SagaStepResult;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    private final SagaProperties.RetryProperties retryProperties;

    public ResilienceConfig(SagaProperties.RetryProperties retryProperties) {
        this.retryProperties = retryProperties;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.<SagaStepResult>custom()
                .maxAttempts(retryProperties.maxAttempts())
                .waitDuration(retryProperties.waitDuration())
                .retryOnResult(new RetryResultPredicate())
                .retryExceptions(Exception.class)
                .build();
        return RetryRegistry.of(config);
    }
}
