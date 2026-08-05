package com.example.device_service.controller;

import com.example.device_service.application.dto.DeviceDto;
import com.example.device_service.application.interfaces.DeviceServiceInterface;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(deviceServiceInterface.getDeviceById(id));
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody DeviceDto deviceDto) {
        DeviceDto createdDevice = deviceServiceInterface.createDevice(deviceDto);
        return new ResponseEntity<>(createdDevice, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable @Positive Long id,
                                                 @Valid @RequestBody DeviceDto deviceDto) {
        return ResponseEntity.ok(deviceServiceInterface.updateDevice(id, deviceDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable @Positive Long id) {
        deviceServiceInterface.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDto>> getAllDevicesByUserId(@PathVariable @Positive Long userId) {
        List<DeviceDto> devices = deviceServiceInterface.getAllDevicesByUserId(userId);
        return ResponseEntity.ok(devices);
    }
}
