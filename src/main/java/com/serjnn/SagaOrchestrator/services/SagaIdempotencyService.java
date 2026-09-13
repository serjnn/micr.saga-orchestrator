package com.serjnn.SagaOrchestrator.services;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface SagaIdempotencyService {
    boolean tryStartSaga(UUID orderId, Duration lockTtl);
    Optional<String> getSagaState(UUID orderId);
    void markCompleted(UUID orderId, Duration ttl);
    void markRolledBack(UUID orderId, Duration ttl);
}
