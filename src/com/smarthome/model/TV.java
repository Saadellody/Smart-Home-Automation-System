package com.smarthome.model;

import com.smarthome.exception.InvalidDeviceOperationException;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;

public class TV extends Device {

    private int channel = 1;
    private int volume = 20;

    public TV(String id, String name, Ecosystem ecosystem) {
        super(id, name, ecosystem);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.TV;
    }

    public void setChannel(int channel) {
        if (channel < 1) {
            throw new InvalidDeviceOperationException("Channel must be 1 or higher");
        }
        this.channel = channel;
        turnOn();
    }

    public void setVolume(int volume) {
        if (volume < 0 || volume > 100) {
            throw new InvalidDeviceOperationException("Volume must be between 0 and 100");
        }
        this.volume = volume;
    }

    public int getChannel() {
        return channel;
    }

    public int getVolume() {
        return volume;
    }
}
