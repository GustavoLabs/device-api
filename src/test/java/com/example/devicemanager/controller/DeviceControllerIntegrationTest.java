package com.example.devicemanager.controller;

import com.example.devicemanager.domain.State;
import com.example.devicemanager.dto.CreateDeviceDTO;
import com.example.devicemanager.dto.DeviceDTO;
import com.example.devicemanager.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeviceControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/api/devices";
        deviceRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/devices - Create device and verify creationTime is set")
    void createDevice_success() {
        CreateDeviceDTO createDTO = CreateDeviceDTO.builder()
                .name("Device 1")
                .brand("Brand A")
                .state(State.AVAILABLE)
                .build();

        ResponseEntity<DeviceDTO> response = restTemplate.postForEntity(baseUrl, createDTO, DeviceDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DeviceDTO created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Device 1");
        assertThat(created.getBrand()).isEqualTo("Brand A");
        assertThat(created.getState()).isEqualTo(State.AVAILABLE);
        assertThat(created.getCreationTime()).isNotNull();

        Instant creationInstant = created.getCreationTime();
        assertThat(creationInstant).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("GET /api/devices/{id} - Get device by id successfully")
    void getDevice_success() {
        CreateDeviceDTO createDTO = CreateDeviceDTO.builder()
                .name("Device Get")
                .brand("Brand G")
                .state(State.INACTIVE)
                .build();

        DeviceDTO created = restTemplate.postForEntity(baseUrl, createDTO, DeviceDTO.class).getBody();
        assertThat(created).isNotNull();

        ResponseEntity<DeviceDTO> response = restTemplate.getForEntity(baseUrl + "/" + created.getId(), DeviceDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DeviceDTO fetched = response.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo("Device Get");
        assertThat(fetched.getState()).isEqualTo(State.INACTIVE);
    }

    @Test
    @DisplayName("GET /api/devices/{id} - Device not found returns 404")
    void getDevice_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /api/devices/{id} - Update device for all states")
    void updateDevice_allStates() {
        CreateDeviceDTO createDTO = CreateDeviceDTO.builder()
                .name("Device Update")
                .brand("Brand U")
                .state(State.AVAILABLE)
                .build();

        DeviceDTO created = restTemplate.postForEntity(baseUrl, createDTO, DeviceDTO.class).getBody();
        assertThat(created).isNotNull();

        for (State state : State.values()) {
            DeviceDTO updateDTO = DeviceDTO.builder()
                    .id(created.getId())
                    .name(created.getName())
                    .brand(created.getBrand())
                    .state(state)
                    .creationTime(created.getCreationTime())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DeviceDTO> entity = new HttpEntity<>(updateDTO, headers);

            ResponseEntity<DeviceDTO> response = restTemplate.exchange(
                    baseUrl + "/" + created.getId(),
                    HttpMethod.PUT,
                    entity,
                    DeviceDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            DeviceDTO updated = response.getBody();
            assertThat(updated).isNotNull();
            assertThat(updated.getState()).isEqualTo(state);
        }
    }

    @Test
    @DisplayName("PUT /api/devices/{id} - Update device not found returns 404")
    void updateDevice_notFound() {
        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(9999L)
                .name("Nonexistent")
                .brand("NoBrand")
                .state(State.AVAILABLE)
                .creationTime(Instant.now())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeviceDTO> entity = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/9999",
                HttpMethod.PUT,
                entity,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("PUT /api/devices/{id} - Cannot update name or brand if device is IN_USE")
    void updateDevice_inUseNameOrBrandFails() {

        CreateDeviceDTO createDTO = CreateDeviceDTO.builder()
                .name("Device InUse")
                .brand("Brand InUse")
                .state(State.IN_USE)
                .build();

        DeviceDTO created = restTemplate.postForEntity(baseUrl, createDTO, DeviceDTO.class).getBody();
        assertThat(created).isNotNull();

        DeviceDTO updateDTO = DeviceDTO.builder()
                .id(created.getId())
                .name("New Name")
                .brand(created.getBrand())
                .state(State.IN_USE)
                .creationTime(created.getCreationTime())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeviceDTO> entity = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.PUT,
                entity,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Cannot update name or brand while device is in use");
    }

    @Test
    @DisplayName("GET /api/devices - Pagination and max page size")
    void listDevices_pagination() {
        for (int i = 0; i < 25; i++) {
            CreateDeviceDTO createDTO = CreateDeviceDTO.builder()
                    .name("Device " + i)
                    .brand("Brand " + (i % 3))
                    .state(State.AVAILABLE)
                    .build();
            restTemplate.postForEntity(baseUrl, createDTO, DeviceDTO.class);
        }

        String url = baseUrl + "?page=0&size=30";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"size\":20");
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} - Delete available device success")
    void deleteDevice_success() {
        CreateDeviceDTO createDTO = CreateDeviceDTO.builder()
                .name("Device To Delete")
                .brand("BrandDel")
                .state(State.AVAILABLE)
                .build();

        DeviceDTO created = restTemplate.postForEntity(baseUrl, createDTO, DeviceDTO.class).getBody();
        assertThat(created).isNotNull();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(deviceRepository.existsById(created.getId())).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} - Cannot delete device in use")
    void deleteDevice_inUseFail() {
        CreateDeviceDTO createDTO = CreateDeviceDTO.builder()
                .name("Device In Use")
                .brand("BrandInUse")
                .state(State.IN_USE)
                .build();

        DeviceDTO created = restTemplate.postForEntity(baseUrl, createDTO, DeviceDTO.class).getBody();
        assertThat(created).isNotNull();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.DELETE,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Cannot delete device that is in use");
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} - Cannot find device")
    void shouldReturn404WhenDeletingNonExistingDevice() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + "9999999",
                HttpMethod.DELETE,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Device not found");
    }
}
