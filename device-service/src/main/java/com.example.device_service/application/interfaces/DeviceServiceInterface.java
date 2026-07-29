package com.example.device_service.application.interfaces;

import com.example.device_service.application.dto.DeviceDto;

import java.util.List;

public interface DeviceServiceInterface {

    DeviceDto getDeviceById(Long id);

    DeviceDto createDevice(DeviceDto request);

    DeviceDto updateDevice(Long id, DeviceDto request);

    void deleteDevice(Long id);

    List<DeviceDto> getAllDevicesByUserId(Long userId);
}
