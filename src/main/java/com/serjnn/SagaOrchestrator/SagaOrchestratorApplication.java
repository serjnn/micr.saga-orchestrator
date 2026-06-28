package com.serjnn.SagaOrchestrator;

import com.serjnn.SagaOrchestrator.config.SagaProperties;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableConfigurationProperties({SagaProperties.ServiceProperties.class, SagaProperties.RetryProperties.class})
public class SagaOrchestratorApplication {

	@Bean
	@LoadBalanced
	public RestClient.Builder restClientBuilder(ObservationRegistry observationRegistry) {
		return RestClient.builder().observationRegistry(observationRegistry);
	}

	public static void main(String[] args) {
		SpringApplication.run(SagaOrchestratorApplication.class, args);
	}
}
