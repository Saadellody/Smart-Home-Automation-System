package com.smarthome.model;

import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;

public class DoorLock extends Device {

    private boolean locked = true;

    public DoorLock(String id, String name, Ecosystem ecosystem) {
        super(id, name, ecosystem);
        turnOn();
    }

    @Override
    public DeviceType getType() {
        return DeviceType.DOOR_LOCK;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public boolean isLocked() {
        return locked;
    }
}
