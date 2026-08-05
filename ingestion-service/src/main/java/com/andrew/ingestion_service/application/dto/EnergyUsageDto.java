package com.andrew.ingestion_service.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;


import java.time.Instant;

@Builder
public record EnergyUsageDto(
        @NotNull(message = "deviceId is required")
        @Positive(message = "deviceId must be greater than 0")
        Long deviceId,

        @NotNull(message = "energyConsumed is required")
        @PositiveOrZero(message = "energyConsumed must be zero or greater")
        Double energyConsumed,

        @NotNull(message = "timestamp is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp) {
}
