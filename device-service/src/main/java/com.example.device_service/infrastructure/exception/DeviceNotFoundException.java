package com.example.device_service.infrastructure.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(Long id) {
        super("Device not found: " + id);
    }
}
