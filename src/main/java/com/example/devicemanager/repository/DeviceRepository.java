package com.example.devicemanager.repository;

import com.example.devicemanager.model.Device;
import com.example.devicemanager.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByBrand(String brand);
    List<Device> findByState(State state);
}
