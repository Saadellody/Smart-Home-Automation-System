package com.smarthome.factory;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Device;
import com.smarthome.model.DoorLock;
import com.smarthome.model.enums.Ecosystem;

public class DoorLockCreator extends DeviceCreator {

    @Override
    protected Device createDevice(String id, String name, SmartHomeFactory family) {
        return new DoorLock(id, name, Ecosystem.GENERIC);
    }
}
