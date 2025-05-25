package com.example.devicemanager.exception;

public class DeviceUpdateNotAllowedException extends RuntimeException {
    public DeviceUpdateNotAllowedException(String message) {
        super(message);
    }
}