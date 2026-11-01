package com.smarthome.exception;

public class DeviceNotFoundException extends ResourceNotFoundException {

    public DeviceNotFoundException(String id) {
        super("Device", id);
    }
}
