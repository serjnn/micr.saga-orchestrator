package com.serjnn.SagaOrchestrator;

import com.serjnn.SagaOrchestrator.config.SagaDefinition;
import com.serjnn.SagaOrchestrator.config.SagaProperties;
import com.serjnn.SagaOrchestrator.dto.BucketItemDTO;
import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.dto.SagaResponseDTO;
import com.serjnn.SagaOrchestrator.dto.SagaStepResult;
import com.serjnn.SagaOrchestrator.services.OrchService;
import com.serjnn.SagaOrchestrator.services.SagaIdempotencyService;
import com.serjnn.SagaOrchestrator.steps.SagaStep;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchServiceTest {

    @Mock
    private SagaIdempotencyService idempotencyService;

    @Mock
    private SagaStep step1;

    @Mock
    private SagaStep step2;

    private OrchService orchService;
    private OrderDTO orderDTO;
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        SagaDefinition definition = new SagaDefinition("Test Saga", List.of(step1, step2));
        SagaProperties.RetryProperties retryProps = new SagaProperties.RetryProperties(
                3, Duration.ofSeconds(1), "Retry", false, true
        );
        RetryRegistry retryRegistry = RetryRegistry.of(RetryConfig.ofDefaults());
        orchService = new OrchService(definition, retryRegistry, retryProps, idempotencyService);

        orderDTO = new OrderDTO(
                orderId,
                1L,
                List.of(new BucketItemDTO(1L, "Item", 1, new BigDecimal("10.00"))),
                new BigDecimal("10.00")
        );
    }

    @Test
    void shouldReturnCachedSuccessWhenSagaAlreadyCompleted() {
        when(idempotencyService.getSagaState(orderId)).thenReturn(Optional.of("COMPLETED"));

        SagaResponseDTO result = orchService.processSaga(orderDTO);

        assertTrue(result.success());
        assertTrue(result.message().contains("idempotent replay"));
        verifyNoInteractions(step1, step2);
    }

    @Test
    void shouldReturnCachedFailureWhenSagaAlreadyRolledBack() {
        when(idempotencyService.getSagaState(orderId)).thenReturn(Optional.of("ROLLED_BACK"));

        SagaResponseDTO result = orchService.processSaga(orderDTO);

        assertFalse(result.success());
        assertTrue(result.message().contains("idempotent replay"));
        verifyNoInteractions(step1, step2);
    }

    @Test
    void shouldReturnInProgressWhenSagaIsCurrentlyLocked() {
        when(idempotencyService.getSagaState(orderId)).thenReturn(Optional.empty());
        when(idempotencyService.tryStartSaga(eq(orderId), any(Duration.class))).thenReturn(false);

        SagaResponseDTO result = orchService.processSaga(orderDTO);

        assertFalse(result.success());
        assertEquals("IN_PROGRESS", result.message());
        verifyNoInteractions(step1, step2);
    }

    @Test
    void shouldExecuteStepsAndMarkCompletedOnSuccess() {
        when(idempotencyService.getSagaState(orderId)).thenReturn(Optional.empty());
        when(idempotencyService.tryStartSaga(eq(orderId), any(Duration.class))).thenReturn(true);
        when(step1.process(orderDTO)).thenReturn(new SagaStepResult.Success());
        when(step2.process(orderDTO)).thenReturn(new SagaStepResult.Success());

        SagaResponseDTO result = orchService.processSaga(orderDTO);

        assertTrue(result.success());
        verify(idempotencyService).markCompleted(eq(orderId), any(Duration.class));
    }
}
