package com.andrew.ingestion_service.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;


import java.time.Instant;

@Builder
public record EnergyUsageDto(
        long deviceId,
        double energyConsumed,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp) {
}
