package com.serjnn.SagaOrchestrator.dto;

import java.util.UUID;

public record SagaResponseDTO(UUID orderId, boolean success, String message) {}
