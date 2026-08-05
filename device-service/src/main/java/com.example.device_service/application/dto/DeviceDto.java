package com.example.device_service.application.dto;

import com.example.device_service.domain.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DeviceDto {
    private Long id;

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    @NotNull(message = "type is required")
    private DeviceType type;

    @NotBlank(message = "location is required")
    @Size(max = 100, message = "location must not exceed 100 characters")
    private String location;

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be greater than 0")
    private Long userId;
}
