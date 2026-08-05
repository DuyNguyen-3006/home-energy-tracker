package com.example.device_service.application.service;

import com.example.device_service.application.dto.DeviceDto;
import com.example.device_service.application.interfaces.DeviceServiceInterface;
import com.example.device_service.domain.entity.Device;
import com.example.device_service.infrastructure.exception.DeviceNotFoundException;
import com.example.device_service.infrastructure.repositories.DeviceRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService implements DeviceServiceInterface {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
        return mapToDto(device);
    }

    @Override
    public DeviceDto createDevice(DeviceDto request) {
        Device device = new Device();
        device.setName(request.getName());
        device.setLocation(request.getLocation());
        device.setType(request.getType());
        device.setUserId(request.getUserId());

        Device savedDevice = deviceRepository.save(device);
        return mapToDto(savedDevice);
    }

    @Override
    public DeviceDto updateDevice(Long id, DeviceDto request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        device.setName(request.getName());
        device.setLocation(request.getLocation());
        device.setType(request.getType());
        device.setUserId(request.getUserId());

        Device updatedDevice = deviceRepository.save(device);
        return mapToDto(updatedDevice);
    }

    @Override
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
        deviceRepository.delete(device);
    }

    public List<DeviceDto> getAllDevicesByUserId(Long userId) {
        try {
            List<Device> devices = deviceRepository.findAllByUserId(userId);
            return devices.stream().map(this::mapToDto).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve devices for userId: " + userId, e);
        }
    }

    public Page<DeviceDto> getAddDevicesByUserId(Long userId, Pageable pageable) {
        try {
            Page<Device> devices = deviceRepository.findAllByUserId(userId, pageable);
            return devices.map(this::mapToDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve devices for userId: " + userId, e);
        }
    }

    private DeviceDto mapToDto(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setType(device.getType());
        dto.setLocation(device.getLocation());
        dto.setUserId(device.getUserId());
        return dto;
    }
}
