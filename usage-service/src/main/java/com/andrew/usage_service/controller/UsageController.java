package com.andrew.usage_service.controller;

import com.andrew.usage_service.dto.UsageDto;
import com.andrew.usage_service.service.UsageService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usage")
public class UsageController {
    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UsageDto> getUserDeviceUsage(
            @PathVariable @Positive(message = "userId must be greater than 0") Long userId,
            @RequestParam(defaultValue = "3")
            @Min(value = 1, message = "days must be at least 1")
            @Max(value = 365, message = "days must not exceed 365") int days) {
       final UsageDto usage = usageService.getXDaysUsageForUser(userId, days);
        return ResponseEntity.ok(usage);
    }
}
