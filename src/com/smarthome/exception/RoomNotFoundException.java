package com.smarthome.exception;

public class RoomNotFoundException extends ResourceNotFoundException {

    public RoomNotFoundException(String id) {
        super("Room", id);
    }
}
