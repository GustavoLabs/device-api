package com.example.devicemanager.repository;

import com.example.devicemanager.domain.Device;
import com.example.devicemanager.domain.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @AfterEach
    void cleanUp() {
        deviceRepository.deleteAll();
    }

    @Test
    void saveAndFindById() {
        Device device = Device.builder()
                .name("TestDevice")
                .brand("TestBrand")
                .state(State.AVAILABLE)
                .creationTime(Instant.now())
                .build();

        Device saved = deviceRepository.save(device);

        Optional<Device> found = deviceRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TestDevice");
    }

    @Test
    void findAllDevices() {
        Device d1 = Device.builder().name("D1").brand("Brand1").state(State.AVAILABLE).creationTime(Instant.now()).build();
        Device d2 = Device.builder().name("D2").brand("Brand2").state(State.IN_USE).creationTime(Instant.now()).build();

        deviceRepository.saveAll(List.of(d1, d2));

        List<Device> allDevices = deviceRepository.findAll();
        assertThat(allDevices).hasSize(2);
    }

    @Test
    void findByBrand() {
        Device d1 = Device.builder().name("D1").brand("BrandA").state(State.AVAILABLE).creationTime(Instant.now()).build();
        Device d2 = Device.builder().name("D2").brand("BrandA").state(State.IN_USE).creationTime(Instant.now()).build();
        Device d3 = Device.builder().name("D3").brand("BrandB").state(State.INACTIVE).creationTime(Instant.now()).build();

        deviceRepository.saveAll(List.of(d1, d2, d3));

        List<Device> brandADevices = deviceRepository.findByBrand("BrandA");
        assertThat(brandADevices).hasSize(2);
        assertThat(brandADevices).allMatch(d -> d.getBrand().equals("BrandA"));
    }

    @Test
    void findByState() {
        Device d1 = Device.builder().name("D1").brand("Brand1").state(State.AVAILABLE).creationTime(Instant.now()).build();
        Device d2 = Device.builder().name("D2").brand("Brand2").state(State.IN_USE).creationTime(Instant.now()).build();
        Device d3 = Device.builder().name("D3").brand("Brand3").state(State.INACTIVE).creationTime(Instant.now()).build();

        deviceRepository.saveAll(List.of(d1, d2, d3));

        List<Device> inUseDevices = deviceRepository.findByState(State.IN_USE);
        assertThat(inUseDevices).hasSize(1);
        assertThat(inUseDevices.get(0).getState()).isEqualTo(State.IN_USE);
    }

    @Test
    void deleteById() {
        Device device = Device.builder()
                .name("ToDelete")
                .brand("BrandDel")
                .state(State.AVAILABLE)
                .creationTime(Instant.now())
                .build();

        Device saved = deviceRepository.save(device);
        deviceRepository.deleteById(saved.getId());

        Optional<Device> found = deviceRepository.findById(saved.getId());
        assertThat(found).isNotPresent();
    }
}
