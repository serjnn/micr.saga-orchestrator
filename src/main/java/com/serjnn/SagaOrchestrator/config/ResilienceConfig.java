package com.serjnn.SagaOrchestrator.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    private final SagaProperties.RetryProperties retryProperties;

    public ResilienceConfig(SagaProperties.RetryProperties retryProperties) {
        this.retryProperties = retryProperties;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.<Boolean>custom()
                .maxAttempts(retryProperties.maxAttempts())
                .waitDuration(retryProperties.waitDuration())
                .retryOnResult(new RetryResultPredicate())
                .retryExceptions(Exception.class)
                .build();
        return RetryRegistry.of(config);
    }
}
