package com.example.device_service.application.interfaces;

import com.example.device_service.application.dto.DeviceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeviceServiceInterface {

    DeviceDto getDeviceById(Long id);

    DeviceDto createDevice(DeviceDto request);

    DeviceDto updateDevice(Long id, DeviceDto request);

    void deleteDevice(Long id);

    List<DeviceDto> getAllDevicesByUserId(Long userId);

    Page<DeviceDto> getDevicesByUserIdPaginated(Long userId, Pageable pageable);
}
