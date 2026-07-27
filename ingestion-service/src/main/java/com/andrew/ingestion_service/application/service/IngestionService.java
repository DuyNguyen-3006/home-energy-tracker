package com.andrew.ingestion_service.application.service;

import com.andrew.ingestion_service.application.dto.EnergyUsageDto;
import com.andrew.kafka.event.EnergyUsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;

    public IngestionService(KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void ingestEnergyUsage(EnergyUsageDto request) {
        EnergyUsageEvent event = EnergyUsageEvent.builder()
            .deviceId(request.deviceId())
            .energyConsumed(request.energyConsumed())
            .timestamp(request.timestamp())
            .build();

        kafkaTemplate.send("energy-usage", event);
        log.info("Ingesty energy usage event: {}", event);
    }
}
