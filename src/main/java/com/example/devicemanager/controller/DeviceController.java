package com.example.devicemanager.controller;

import com.example.devicemanager.dto.CreateDeviceDTO;
import com.example.devicemanager.dto.DeviceDTO;
import com.example.devicemanager.domain.State;
import com.example.devicemanager.service.DeviceService;
import com.example.devicemanager.service.DeviceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;

    @Operation(summary = "Create a new device")
    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody CreateDeviceDTO deviceDTO) {
        log.info("Creating device: {}", deviceDTO.getName());
        DeviceDTO createdDTO = deviceService.createDevice(deviceDTO);
        return ResponseEntity.ok(createdDTO);
    }

    @Operation(summary = "Update existing device")
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceDTO deviceDTO) {
        log.info("Updating device id={}", id);
        DeviceDTO updatedDTO = deviceService.updateDevice(id, deviceDTO);
        return ResponseEntity.ok(updatedDTO);
    }

    @Operation(summary = "Get device by id")
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable Long id) {
        log.info("Fetching device id={}", id);
        DeviceDTO dto = deviceService.getDevice(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(summary = "Get paged list of devices")
    public Page<DeviceDTO> getPagedDevices(
            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default = 10, max = 20)")
            @RequestParam(defaultValue = "10") int size
    ) {
        return deviceService.listDevices(PageRequest.of(page, size));
    }

    @Operation(summary = "Get devices by brand")
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByBrand(@PathVariable String brand) {
        log.info("Fetching devices by brand={}", brand);
        List<DeviceDTO> dtos = deviceService.getDevicesByBrand(brand);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get devices by state")
    @GetMapping("/state/{state}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByState(@PathVariable State state) {
        log.info("Fetching devices by state={}", state);
        List<DeviceDTO> dtos = deviceService.getDevicesByState(state);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Delete device by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.info("Deleting device id={}", id);
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create multiple devices in bulk")
    @PostMapping("/bulk")
    public ResponseEntity<List<DeviceDTO>> createDevicesBulk(@Valid @RequestBody List<DeviceDTO> deviceDTOs) {
        log.info("Creating bulk devices, count={}", deviceDTOs.size());
        List<DeviceDTO> createdDevices = deviceService.createDevicesBulk(deviceDTOs);
        return ResponseEntity.ok(createdDevices);
    }
}
