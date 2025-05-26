package com.example.devicemanager.dto;

import com.example.devicemanager.model.State;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDTO implements Serializable {

    private Long id;

    private String name;

    private String brand;

    private State state;

    private Instant creationTime;
}
