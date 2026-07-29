package com.example.device_service.controller;

import com.example.device_service.application.dto.DeviceDto;
import com.example.device_service.application.interfaces.DeviceServiceInterface;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/device")
@Tag(name = "Devices", description = "Device Management APIs")
public class DeviceController {

    private final DeviceServiceInterface deviceServiceInterface;

    public DeviceController(DeviceServiceInterface deviceServiceInterface) {
        this.deviceServiceInterface = deviceServiceInterface;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable Long id) {
        try {
            DeviceDto deviceDto = deviceServiceInterface.getDeviceById(id);
            return ResponseEntity.ok(deviceDto);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@RequestBody DeviceDto deviceDto) {
        DeviceDto createdDevice = deviceServiceInterface.createDevice(deviceDto);
        return new ResponseEntity<>(createdDevice, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable Long id, @RequestBody DeviceDto deviceDto) {
        try {
            DeviceDto updatedDevice = deviceServiceInterface.updateDevice(id, deviceDto);
            return ResponseEntity.ok(updatedDevice);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        try {
            deviceServiceInterface.deleteDevice(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDto>> getAllDevicesByUserId(@PathVariable Long userId) {
        List<DeviceDto> devices = deviceServiceInterface.getAllDevicesByUserId(userId);
        return ResponseEntity.ok(devices);
    }
}
