package com.example.device_service.application.interfaces;

import com.example.device_service.application.dto.DeviceDto;

public interface DeviceServiceInterface {

    DeviceDto getDeviceById(Long id);

    DeviceDto createDevice(DeviceDto request);

    DeviceDto updateDevice(Long id, DeviceDto request);

    void deleteDevice(Long id);
}
