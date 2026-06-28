package com.serjnn.SagaOrchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

public class SagaProperties {

    @ConfigurationProperties(prefix = "services")
    public record ServiceProperties(
            BucketProperties bucket,
            ClientProperties client,
            OrderProperties order
    ) {
        public record BucketProperties(String clearUrl, String restoreUrl) {}
        public record ClientProperties(String deductUrl, String restoreUrl) {}
        public record OrderProperties(String createUrl, String removeUrl) {}
    }

    @ConfigurationProperties(prefix = "resilience.retry")
    public record RetryProperties(
            int maxAttempts,
            Duration waitDuration,
            String suffix,
            boolean processEnabled,
            boolean revertEnabled
    ) {}
}
