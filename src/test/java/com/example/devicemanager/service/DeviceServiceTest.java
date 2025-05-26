package com.example.devicemanager.service;

import com.example.devicemanager.model.Device;
import com.example.devicemanager.model.State;
import com.example.devicemanager.dto.CreateDeviceDTO;
import com.example.devicemanager.dto.DeviceDTO;
import com.example.devicemanager.exception.BusinessException;
import com.example.devicemanager.exception.DeviceInUseException;
import com.example.devicemanager.exception.DeviceNotFoundException;
import com.example.devicemanager.repository.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DeviceServiceTest {

    @InjectMocks
    private DeviceService deviceService;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    // === CREATE DEVICE ===
    @Test
    @DisplayName("Create device - success")
    void createDevice_success() {
        CreateDeviceDTO createDTO = new CreateDeviceDTO("device1", "brand1", State.AVAILABLE);
        Device deviceEntity = new Device();
        deviceEntity.setName("device1");
        deviceEntity.setBrand("brand1");
        deviceEntity.setState(State.AVAILABLE);

        Device savedDevice = new Device();
        savedDevice.setId(1L);
        savedDevice.setName("device1");
        savedDevice.setBrand("brand1");
        savedDevice.setState(State.AVAILABLE);
        savedDevice.setCreationTime(Instant.now());

        DeviceDTO expectedDTO = DeviceDTO.builder().id(1L).name("device1").brand("brand1").state(State.AVAILABLE).build();

        when(deviceMapper.toEntity(createDTO)).thenReturn(deviceEntity);
        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);
        when(deviceMapper.toDTO(savedDevice)).thenReturn(expectedDTO);

        DeviceDTO result = deviceService.createDevice(createDTO);

        assertThat(result).isEqualTo(expectedDTO);
        verify(deviceRepository).save(any(Device.class));
    }

    // === UPDATE DEVICE - success with different states ===
    @Test
    @DisplayName("Update device - success, all states allowed except name/brand change in IN_USE")
    void updateDevice_success_allStates() {
        Long id = 1L;

        Device existing = new Device();
        existing.setId(id);
        existing.setName("oldName");
        existing.setBrand("oldBrand");
        existing.setState(State.AVAILABLE);
        existing.setCreationTime(Instant.now());

        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(id)
                .name("newName")
                .brand("newBrand")
                .state(State.INACTIVE)
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deviceMapper.toDTO(any(Device.class))).thenReturn(updateDTO);

        DeviceDTO updated = deviceService.updateDevice(id, updateDTO);
        assertThat(updated.getName()).isEqualTo("newName");
        assertThat(updated.getBrand()).isEqualTo("newBrand");
        assertThat(updated.getState()).isEqualTo(State.INACTIVE);
    }

    @Test
    @DisplayName("Update device - throw BusinessException if device not found")
    void updateDevice_notFound() {
        Long id = 999L;
        when(deviceRepository.findById(id)).thenReturn(Optional.empty());

        DeviceDTO dto = DeviceDTO.builder().name("name").brand("brand").state(State.AVAILABLE).build();

        assertThatThrownBy(() -> deviceService.updateDevice(id, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Update device - throw BusinessException if IN_USE and try to change name or brand")
    void updateDevice_inUseNameBrandChange() {
        Long id = 1L;

        Device existing = new Device();
        existing.setId(id);
        existing.setName("oldName");
        existing.setBrand("oldBrand");
        existing.setState(State.IN_USE);
        existing.setCreationTime(Instant.now());

        // Change name
        DeviceDTO dto1 = DeviceDTO.builder().id(id).name("newName").brand("oldBrand").state(State.IN_USE).build();

        // Change brand
        DeviceDTO dto2 = DeviceDTO.builder().id(id).name("oldName").brand("newBrand").state(State.IN_USE).build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> deviceService.updateDevice(id, dto1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot update name or brand");

        assertThatThrownBy(() -> deviceService.updateDevice(id, dto2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot update name or brand");
    }

    // === GET DEVICE ===
    @Test
    @DisplayName("Get device - success")
    void getDevice_success() {
        Long id = 1L;
        Device device = new Device();
        device.setId(id);
        device.setName("device");
        device.setBrand("brand");
        device.setState(State.AVAILABLE);

        DeviceDTO dto = DeviceDTO.builder().id(id).name("device").brand("brand").state(State.AVAILABLE).build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));
        when(deviceMapper.toDTO(device)).thenReturn(dto);

        DeviceDTO result = deviceService.getDevice(id);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Get device - throw DeviceNotFoundException if not found")
    void getDevice_notFound() {
        Long id = 999L;
        when(deviceRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> deviceService.getDevice(id))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining("Device not found");
    }

    // === DELETE DEVICE ===
    @Test
    @DisplayName("Delete device - success when state is not IN_USE")
    void deleteDevice_success() {
        Long id = 1L;
        Device device = new Device();
        device.setId(id);
        device.setState(State.AVAILABLE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));
        doNothing().when(deviceRepository).deleteById(id);

        deviceService.deleteDevice(id);

        verify(deviceRepository).deleteById(id);
    }

    @Test
    @DisplayName("Delete device - throw DeviceInUseException when device is IN_USE")
    void deleteDevice_inUse() {
        Long id = 1L;
        Device device = new Device();
        device.setId(id);
        device.setState(State.IN_USE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.deleteDevice(id))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot delete device that is in use");

        verify(deviceRepository, never()).deleteById(id);
    }


    // === GET DEVICES BY BRAND ===
    @Test
    @DisplayName("Get devices by brand - success")
    void getDevicesByBrand_success() {
        List<Device> devices = List.of(new Device(), new Device());
        when(deviceRepository.findByBrand("brand1")).thenReturn(devices);
        when(deviceMapper.toDTO(any())).thenReturn(DeviceDTO.builder().build());

        List<DeviceDTO> dtos = deviceService.getDevicesByBrand("brand1");
        assertThat(dtos.size()).isEqualTo(devices.size());
    }

    // === GET DEVICES BY STATE ===
    @Test
    @DisplayName("Get devices by state - success for all states")
    void getDevicesByState_success() {
        for (State state : State.values()) {
            List<Device> devices = List.of(new Device(), new Device());
            when(deviceRepository.findByState(state)).thenReturn(devices);
            when(deviceMapper.toDTO(any())).thenReturn(DeviceDTO.builder().build());

            List<DeviceDTO> dtos = deviceService.getDevicesByState(state);
            assertThat(dtos.size()).isEqualTo(devices.size());

            clearInvocations(deviceRepository, deviceMapper);
        }
    }

    // === CREATE DEVICES BULK ===
    @Test
    @DisplayName("Create devices bulk - success")
    void createDevicesBulk_success() {
        DeviceDTO dto1 = DeviceDTO.builder().name("d1").brand("b1").state(State.AVAILABLE).build();
        DeviceDTO dto2 = DeviceDTO.builder().name("d2").brand("b2").state(State.INACTIVE).build();

        List<DeviceDTO> dtos = List.of(dto1, dto2);

        List<Device> entities = List.of(new Device(), new Device());
        List<Device> saved = List.of(new Device(), new Device());

        when(deviceMapper.toEntity(any(DeviceDTO.class))).thenReturn(new Device());
        when(deviceRepository.saveAll(anyList())).thenReturn(saved);
        when(deviceMapper.toDTO(any(Device.class))).thenReturn(DeviceDTO.builder().build());

        List<DeviceDTO> result = deviceService.createDevicesBulk(dtos);
        assertThat(result.size()).isEqualTo(dtos.size());
    }

    @Test
    @DisplayName("Update device - change only name with AVAILABLE state should succeed")
    void updateDevice_changeOnlyName_available() {
        Long id = 10L;
        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.AVAILABLE);
        existing.setCreationTime(Instant.now());

        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(id)
                .name("NewName")
                .brand("OldBrand")
                .state(State.AVAILABLE)
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));
        when(deviceMapper.toDTO(any(Device.class))).thenReturn(updateDTO);

        DeviceDTO result = deviceService.updateDevice(id, updateDTO);

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getBrand()).isEqualTo("OldBrand");
    }

    @Test
    @DisplayName("Update device - change only brand with AVAILABLE state should succeed")
    void updateDevice_changeOnlyBrand_available() {
        Long id = 11L;
        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.AVAILABLE);
        existing.setCreationTime(Instant.now());

        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(id)
                .name("OldName")
                .brand("NewBrand")
                .state(State.AVAILABLE)
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));
        when(deviceMapper.toDTO(any(Device.class))).thenReturn(updateDTO);

        DeviceDTO result = deviceService.updateDevice(id, updateDTO);

        assertThat(result.getName()).isEqualTo("OldName");
        assertThat(result.getBrand()).isEqualTo("NewBrand");
    }

    @Test
    @DisplayName("Update device - change only name with IN_USE state should throw BusinessException")
    void updateDevice_changeOnlyName_inUse_throws() {
        Long id = 12L;
        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.IN_USE);
        existing.setCreationTime(Instant.now());

        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(id)
                .name("NewName")
                .brand("OldBrand")
                .state(State.IN_USE)
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> deviceService.updateDevice(id, updateDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot update name or brand");
    }

    @Test
    @DisplayName("Update device - change only brand with IN_USE state should throw BusinessException")
    void updateDevice_changeOnlyBrand_inUse_throws() {
        Long id = 13L;
        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.IN_USE);
        existing.setCreationTime(Instant.now());

        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(id)
                .name("OldName")
                .brand("NewBrand")
                .state(State.IN_USE)
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> deviceService.updateDevice(id, updateDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot update name or brand");
    }

    @Test
    @DisplayName("Update device - change only name with INACTIVE state should succeed")
    void updateDevice_changeOnlyName_inactive() {
        Long id = 14L;
        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.INACTIVE);
        existing.setCreationTime(Instant.now());

        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(id)
                .name("NewName")
                .brand("OldBrand")
                .state(State.INACTIVE)
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));
        when(deviceMapper.toDTO(any(Device.class))).thenReturn(updateDTO);

        DeviceDTO result = deviceService.updateDevice(id, updateDTO);

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getBrand()).isEqualTo("OldBrand");
    }

    @Test
    @DisplayName("Update device - change only brand with INACTIVE state should succeed")
    void updateDevice_changeOnlyBrand_inactive() {
        Long id = 15L;
        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.INACTIVE);
        existing.setCreationTime(Instant.now());

        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(id)
                .name("OldName")
                .brand("NewBrand")
                .state(State.INACTIVE)
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));
        when(deviceMapper.toDTO(any(Device.class))).thenReturn(updateDTO);

        DeviceDTO result = deviceService.updateDevice(id, updateDTO);

        assertThat(result.getName()).isEqualTo("OldName");
        assertThat(result.getBrand()).isEqualTo("NewBrand");
    }

}
