package com.example.devicemanager.service;

import com.example.devicemanager.model.Device;
import com.example.devicemanager.dto.CreateDeviceDTO;
import com.example.devicemanager.dto.DeviceDTO;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public DeviceDTO toDTO(Device device) {
        return DeviceDTO.builder()
                .id(device.getId())
                .name(device.getName())
                .brand(device.getBrand())
                .state(device.getState())
                .creationTime(device.getCreationTime() != null ? device.getCreationTime() : null)
                .build();
    }

    public Device toEntity(DeviceDTO dto) {
        Device device = new Device();
        device.setId(dto.getId());
        device.setName(dto.getName());
        device.setBrand(dto.getBrand());
        device.setState(dto.getState());
        return device;
    }

    public Device toEntity(CreateDeviceDTO createDeviceDTO) {
        if (createDeviceDTO == null) return null;

        Device device = new Device();
        device.setName(createDeviceDTO.getName());
        device.setBrand(createDeviceDTO.getBrand());
        device.setState(createDeviceDTO.getState());
        return device;
    }
}
