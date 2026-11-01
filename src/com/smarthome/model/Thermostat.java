package com.smarthome.model;

import com.smarthome.exception.InvalidDeviceOperationException;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;

public class Thermostat extends Device {

    public static final double MIN_TEMPERATURE = 5;
    public static final double MAX_TEMPERATURE = 35;

    /** Target temperature, always stored in Celsius. */
    private double targetTemperature = 21;

    public Thermostat(String id, String name, Ecosystem ecosystem) {
        super(id, name, ecosystem);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.THERMOSTAT;
    }

    public void setTargetTemperature(double celsius) {
        if (celsius < MIN_TEMPERATURE || celsius > MAX_TEMPERATURE) {
            throw new InvalidDeviceOperationException(
                    "Temperature must be between " + MIN_TEMPERATURE + " and " + MAX_TEMPERATURE + " C");
        }
        this.targetTemperature = celsius;
        turnOn();
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }
}
