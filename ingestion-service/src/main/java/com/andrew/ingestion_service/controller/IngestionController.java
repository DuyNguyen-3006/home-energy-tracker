package com.andrew.ingestion_service.controller;

import com.andrew.ingestion_service.application.dto.EnergyUsageDto;
import com.andrew.ingestion_service.application.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void ingestData(@Valid @RequestBody EnergyUsageDto request) {
        ingestionService.ingestEnergyUsage(request);
    }
}
