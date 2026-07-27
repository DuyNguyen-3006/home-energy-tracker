package com.andrew.usage_service.dto;

import lombok.Builder;

@Builder
public record UserDto(Long id,
                      String firstName,
                      String lastName,
                      String email,
                      String address,
                      boolean alerting,
                      double energyAlertingThreshold) {
}
