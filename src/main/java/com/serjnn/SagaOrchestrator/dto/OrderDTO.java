package com.serjnn.SagaOrchestrator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDTO(
        @NotNull UUID orderId,
        @NotNull Long clientId,
        @NotEmpty List<@Valid BucketItemDTO> items,
        @NotNull @Positive BigDecimal totalSum
) {}
