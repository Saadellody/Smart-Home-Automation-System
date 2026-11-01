package com.smarthome.exception;

public class ResourceNotFoundException extends SmartHomeException {

    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found: " + id);
    }
}
