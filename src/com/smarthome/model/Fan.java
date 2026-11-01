package com.smarthome.model;

import com.smarthome.exception.InvalidDeviceOperationException;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;

public class Fan extends Device {

    public static final int MAX_SPEED = 5;

    private int speed = 1;

    public Fan(String id, String name, Ecosystem ecosystem) {
        super(id, name, ecosystem);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.FAN;
    }

    public void setSpeed(int speed) {
        if (speed < 1 || speed > MAX_SPEED) {
            throw new InvalidDeviceOperationException("Fan speed must be between 1 and " + MAX_SPEED);
        }
        this.speed = speed;
        turnOn();
    }

    public int getSpeed() {
        return speed;
    }
}
