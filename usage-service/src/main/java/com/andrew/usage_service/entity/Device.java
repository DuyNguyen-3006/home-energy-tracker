package com.andrew.usage_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Device {
    Long id;
    String name;
    String type;
    String location;
    Long userId;
    double energyConsumed;
}
