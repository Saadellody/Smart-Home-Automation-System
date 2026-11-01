package com.smarthome.factory;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Device;

public class ThermostatCreator extends DeviceCreator {

    @Override
    protected Device createDevice(String id, String name, SmartHomeFactory family) {
        return family.createThermostat(id, name);
    }
}
