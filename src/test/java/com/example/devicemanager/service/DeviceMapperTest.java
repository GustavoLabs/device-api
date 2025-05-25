package com.example.devicemanager.service;

import com.example.devicemanager.domain.Device;
import com.example.devicemanager.domain.State;
import com.example.devicemanager.dto.CreateDeviceDTO;
import com.example.devicemanager.dto.DeviceDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceMapperTest {

    private final DeviceMapper mapper = new DeviceMapper();

    @Test
    @DisplayName("Map Device to DeviceDTO - success")
    void toDTO_success() {
        Device device = new Device();
        device.setId(1L);
        device.setName("name");
        device.setBrand("brand");
        device.setState(State.AVAILABLE);
        device.setCreationTime(Instant.parse("2025-05-25T10:15:30Z"));

        DeviceDTO dto = mapper.toDTO(device);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("name");
        assertThat(dto.getBrand()).isEqualTo("brand");
        assertThat(dto.getState()).isEqualTo(State.AVAILABLE);
        assertThat(dto.getCreationTime()).isEqualTo("2025-05-25T10:15:30Z");
    }

    @Test
    @DisplayName("Map DeviceDTO to Device - success")
    void toEntity_fromDTO() {
        DeviceDTO dto = DeviceDTO.builder()
                .id(2L)
                .name("name2")
                .brand("brand2")
                .state(State.IN_USE)
                .build();

        Device device = mapper.toEntity(dto);

        assertThat(device.getId()).isEqualTo(2L);
        assertThat(device.getName()).isEqualTo("name2");
        assertThat(device.getBrand()).isEqualTo("brand2");
        assertThat(device.getState()).isEqualTo(State.IN_USE);
    }

    @Test
    @DisplayName("Map CreateDeviceDTO to Device - success")
    void toEntity_fromCreateDTO() {
        CreateDeviceDTO createDTO = new CreateDeviceDTO("name3", "brand3", State.INACTIVE);

        Device device = mapper.toEntity(createDTO);

        assertThat(device.getName()).isEqualTo("name3");
        assertThat(device.getBrand()).isEqualTo("brand3");
        assertThat(device.getState()).isEqualTo(State.INACTIVE);
    }
}
