package com.serjnn.SagaOrchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record BucketItemDTO(
        @NotNull Long id,
        @NotBlank String name,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal price
) {}
