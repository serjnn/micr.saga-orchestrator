package com.serjnn.SagaOrchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serjnn.SagaOrchestrator.dto.BucketItemDTO;
import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import io.micrometer.observation.ObservationRegistry;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "services.client.deduct-url=http://localhost:${wiremock.server.port}/api/v1/deduct",
    "services.client.restore-url=http://localhost:${wiremock.server.port}/api/v1/client/restore",
    "services.bucket.clear-url=http://localhost:${wiremock.server.port}/api/v1/clear",
    "services.bucket.restore-url=http://localhost:${wiremock.server.port}/api/v1/bucket/restore",
    "services.order.create-url=http://localhost:${wiremock.server.port}/api/v1/create",
    "services.order.remove-url=http://localhost:${wiremock.server.port}/api/v1/remove",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.loadbalancer.enabled=false",
    "spring.main.allow-bean-definition-overriding=true"
})
public class SagaOrchestratorIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RestClient.Builder restClientBuilder(ObservationRegistry observationRegistry) {
            return RestClient.builder().observationRegistry(observationRegistry);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        orderDTO = new OrderDTO(
                UUID.randomUUID(),
                123L,
                List.of(new BucketItemDTO(1L, "Product", 2, new BigDecimal("50.00"))),
                new BigDecimal("100.00")
        );
        WireMock.reset();
    }

    @Test
    void testSuccessfulSaga() throws Exception {
        // Step 1: ClientBalanceStep
        stubFor(WireMock.post(urlEqualTo("/api/v1/deduct"))
                .willReturn(aResponse().withStatus(200)));

        // Step 2: BucketStep
        stubFor(WireMock.post(urlEqualTo("/api/v1/clear"))
                .willReturn(aResponse().withStatus(200)));

        // Step 3: OrderStep
        stubFor(WireMock.post(urlEqualTo("/api/v1/create"))
                .willReturn(aResponse().withStatus(200)));

        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(postRequestedFor(urlEqualTo("/api/v1/deduct")));
        verify(postRequestedFor(urlEqualTo("/api/v1/clear")));
        verify(postRequestedFor(urlEqualTo("/api/v1/create")));
    }

    @Test
    void testSagaRollbackOnStep2Failure() throws Exception {
        // Step 1: ClientBalanceStep (Success)
        stubFor(WireMock.post(urlEqualTo("/api/v1/deduct"))
                .willReturn(aResponse().withStatus(200)));

        // Step 2: BucketStep (Failure)
        stubFor(WireMock.post(urlEqualTo("/api/v1/clear"))
                .willReturn(aResponse().withStatus(500)));

        // Rollback Step 1: ClientBalanceStep
        stubFor(WireMock.post(urlEqualTo("/api/v1/client/restore"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("true")));

        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(postRequestedFor(urlEqualTo("/api/v1/deduct")));
        verify(postRequestedFor(urlEqualTo("/api/v1/clear")));
        verify(postRequestedFor(urlEqualTo("/api/v1/client/restore")));
        verify(0, postRequestedFor(urlEqualTo("/api/v1/create")));
    }

    @Test
    void testSagaRollbackOnStep3Failure() throws Exception {
        // Step 1: ClientBalanceStep (Success)
        stubFor(WireMock.post(urlEqualTo("/api/v1/deduct"))
                .willReturn(aResponse().withStatus(200)));

        // Step 2: BucketStep (Success)
        stubFor(WireMock.post(urlEqualTo("/api/v1/clear"))
                .willReturn(aResponse().withStatus(200)));

        // Step 3: OrderStep (Failure)
        stubFor(WireMock.post(urlEqualTo("/api/v1/create"))
                .willReturn(aResponse().withStatus(500)));

        // Rollback Step 2: BucketStep
        stubFor(WireMock.post(urlEqualTo("/api/v1/bucket/restore"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("true")));

        // Rollback Step 1: ClientBalanceStep
        stubFor(WireMock.post(urlEqualTo("/api/v1/client/restore"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("true")));

        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(postRequestedFor(urlEqualTo("/api/v1/deduct")));
        verify(postRequestedFor(urlEqualTo("/api/v1/clear")));
        verify(postRequestedFor(urlEqualTo("/api/v1/create")));
        verify(postRequestedFor(urlEqualTo("/api/v1/bucket/restore")));
        verify(postRequestedFor(urlEqualTo("/api/v1/client/restore")));
    }
}
