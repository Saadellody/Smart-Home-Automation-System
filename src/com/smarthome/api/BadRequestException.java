package com.smarthome.api;

/** Malformed or incomplete request payload; answered with HTTP 400. */
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(message);
    }
}
