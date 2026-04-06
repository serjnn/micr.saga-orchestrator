package com.serjnn.SagaOrchestrator;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
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
