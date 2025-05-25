package com.example.devicemanager.service;

import com.example.devicemanager.domain.Device;
import com.example.devicemanager.domain.State;
import com.example.devicemanager.dto.CreateDeviceDTO;
import com.example.devicemanager.dto.DeviceDTO;
import com.example.devicemanager.exception.BusinessException;
import com.example.devicemanager.exception.DeviceInUseException;
import com.example.devicemanager.exception.DeviceNotFoundException;
import com.example.devicemanager.repository.DeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    @Autowired
    private final DeviceRepository deviceRepository;

    private final DeviceMapper deviceMapper;

    @PostConstruct
    public void initCache() {
    }
    private static final int MAX_PAGE_SIZE = 20;

    @CachePut(value = "devices", key = "#result.id")
    @CacheEvict(value = "allDevices", allEntries = true)
    public DeviceDTO createDevice(CreateDeviceDTO deviceDTO) {
        Device device = deviceMapper.toEntity(deviceDTO);
        device.setCreationTime(Instant.now());
        Device saved = deviceRepository.save(device);
        return deviceMapper.toDTO(saved);
    }

    @CachePut(value = "devices", key = "#id")
    @CacheEvict(value = "allDevices", allEntries = true)
    public DeviceDTO updateDevice(Long id, DeviceDTO deviceDTO) {
        Device existing = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Device with id " + id + " not found"));

        if (existing.getState() == State.IN_USE) {
            if (deviceDTO.getName() != null && !deviceDTO.getName().equals(existing.getName())
                    || deviceDTO.getBrand() != null && !deviceDTO.getBrand().equals(existing.getBrand())) {
                throw new BusinessException("Cannot update name or brand while device is in use");
            }
        }

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        HashMap<String, Object> existingMap = objectMapper.convertValue(existing, HashMap.class);
        Map dtoMap = objectMapper.convertValue(deviceDTO, Map.class);

        for (Map.Entry<String, Object> entry : existingMap.entrySet()) {
            Object newValue = dtoMap.get(entry.getKey());
            if (newValue != null) {
                entry.setValue(newValue);
            }
        }

        Device updated = objectMapper.convertValue(existingMap, Device.class);
        updated.setCreationTime(existing.getCreationTime());

        updated = deviceRepository.save(updated);

        return deviceMapper.toDTO(updated);
    }

    @Cacheable(value = "devices", key = "#id")
    public DeviceDTO getDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));
        return deviceMapper.toDTO(device);
    }

    @CacheEvict(value = "allDevices", allEntries = true)
    public List<DeviceDTO> createDevicesBulk(List<DeviceDTO> deviceDTOs) {
        Instant now = Instant.now();
        List<Device> devices = deviceDTOs.stream()
                .peek(dto -> dto.setCreationTime(now))
                .map(deviceMapper::toEntity)
                .collect(Collectors.toList());
        List<Device> savedDevices = deviceRepository.saveAll(devices);
        return savedDevices.stream()
                .map(deviceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "allDevices")
    public Page<DeviceDTO> listDevices(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);

        Pageable validatedPageable = PageRequest.of(page, size);
        return deviceRepository.findAll(validatedPageable)
                .map(deviceMapper::toDTO);
    }

    public List<DeviceDTO> getDevicesByBrand(String brand) {
        List<Device> devices = deviceRepository.findByBrand(brand);
        return devices.stream()
                .map(deviceMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceDTO> getDevicesByState(State state) {
        List<Device> devices = deviceRepository.findByState(state);
        return devices.stream()
                .map(deviceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "devices", key = "#id"),
            @CacheEvict(value = "devicesAll", allEntries = true)
    })
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

        if (device.getState() == State.IN_USE) {
            throw new DeviceInUseException("Cannot delete device that is in use");
        }
        deviceRepository.deleteById(id);
    }
}
