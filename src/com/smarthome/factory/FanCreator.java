package com.smarthome.factory;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Device;
import com.smarthome.model.Fan;
import com.smarthome.model.enums.Ecosystem;

public class FanCreator extends DeviceCreator {

    @Override
    protected Device createDevice(String id, String name, SmartHomeFactory family) {
        return new Fan(id, name, Ecosystem.GENERIC);
    }
}
