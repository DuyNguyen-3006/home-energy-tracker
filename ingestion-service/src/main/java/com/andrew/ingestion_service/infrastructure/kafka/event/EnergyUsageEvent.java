package com.andrew.ingestion_service.infrastructure.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;
@Builder
public record EnergyUsageEvent(
    long deviceId,
    double energyConsumed,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant timestamp) {}
