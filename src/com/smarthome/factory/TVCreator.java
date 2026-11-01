package com.smarthome.factory;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Device;
import com.smarthome.model.TV;
import com.smarthome.model.enums.Ecosystem;

public class TVCreator extends DeviceCreator {

    @Override
    protected Device createDevice(String id, String name, SmartHomeFactory family) {
        return new TV(id, name, Ecosystem.GENERIC);
    }
}
