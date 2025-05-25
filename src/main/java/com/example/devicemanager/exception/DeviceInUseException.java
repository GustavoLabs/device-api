package com.example.devicemanager.exception;

import org.springframework.http.HttpStatus;

public class DeviceInUseException extends ApiException {
    public DeviceInUseException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}