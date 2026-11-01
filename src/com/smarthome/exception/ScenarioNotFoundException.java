package com.smarthome.exception;

public class ScenarioNotFoundException extends ResourceNotFoundException {

    public ScenarioNotFoundException(String id) {
        super("Scenario", id);
    }
}
