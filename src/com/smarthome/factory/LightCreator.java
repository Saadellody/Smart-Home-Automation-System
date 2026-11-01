package com.smarthome.factory;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Device;

public class LightCreator extends DeviceCreator {

    @Override
    protected Device createDevice(String id, String name, SmartHomeFactory family) {
        return family.createLight(id, name);
    }
}
