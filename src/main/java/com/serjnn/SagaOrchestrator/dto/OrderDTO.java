package com.serjnn.SagaOrchestrator.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDTO(UUID orderId, Long clientID, List<BucketItemDTO> items, BigDecimal totalSum) {}
