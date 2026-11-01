package com.smarthome.model;

import com.smarthome.exception.InvalidDeviceOperationException;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;

public class Light extends Device {

    private int brightness = 100;

    public Light(String id, String name, Ecosystem ecosystem) {
        super(id, name, ecosystem);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.LIGHT;
    }

    /** Brightness in percent. 0 turns the light off, any other value turns it on. */
    public void setBrightness(int brightness) {
        if (brightness < 0 || brightness > 100) {
            throw new InvalidDeviceOperationException("Brightness must be between 0 and 100");
        }
        this.brightness = brightness;
        if (brightness == 0) {
            turnOff();
        } else {
            turnOn();
        }
    }

    /** A light dimmed to 0% comes back at full brightness when switched on. */
    @Override
    public void turnOn() {
        if (brightness == 0) {
            brightness = 100;
        }
        super.turnOn();
    }

    public int getBrightness() {
        return brightness;
    }
}
