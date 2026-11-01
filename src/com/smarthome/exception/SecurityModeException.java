package com.smarthome.exception;

/** Raised when an action would weaken the home's protection while security mode is armed. */
public class SecurityModeException extends InvalidDeviceOperationException {

    public SecurityModeException(String message) {
        super(message);
    }
}
