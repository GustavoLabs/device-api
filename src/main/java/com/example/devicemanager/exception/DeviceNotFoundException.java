package com.example.devicemanager.exception;

import org.springframework.http.HttpStatus;

public class DeviceNotFoundException extends ApiException {
    public DeviceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}