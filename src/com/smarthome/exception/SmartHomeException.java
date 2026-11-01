package com.smarthome.exception;

/** Base class of every business error raised by the system. */
public class SmartHomeException extends RuntimeException {

    public SmartHomeException(String message) {
        super(message);
    }
}
