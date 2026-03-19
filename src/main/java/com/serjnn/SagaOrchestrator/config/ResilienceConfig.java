package com.serjnn.SagaOrchestrator.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.<Boolean>custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryOnResult(result -> result != null && !result)
                .retryExceptions(Exception.class)
                .build();
        return RetryRegistry.of(config);
    }
}
