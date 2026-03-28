package com.serjnn.SagaOrchestrator.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Value("${resilience.retry.max-attempts}")
    private int maxAttempts;

    @Value("${resilience.retry.wait-duration}")
    private Duration waitDuration;

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.<Boolean>custom()
                .maxAttempts(maxAttempts)
                .waitDuration(waitDuration)
                .retryOnResult(new RetryResultPredicate())
                .retryExceptions(Exception.class)
                .build();
        return RetryRegistry.of(config);
    }
}
