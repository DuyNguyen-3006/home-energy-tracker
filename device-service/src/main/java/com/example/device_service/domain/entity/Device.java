package com.example.device_service.domain.entity;

import com.example.device_service.domain.enums.DeviceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DeviceType type;
    private String location;
    private Long userId;

}

