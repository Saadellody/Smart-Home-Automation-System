package com.smarthome.abstractfactory.apple;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Camera;
import com.smarthome.model.Light;
import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.Ecosystem;

/** Creates the compatible family of Apple devices. */
public class AppleHomeFactory implements SmartHomeFactory {

    @Override
    public Ecosystem getEcosystem() {
        return Ecosystem.APPLE;
    }

    @Override
    public Light createLight(String id, String name) {
        return new AppleLight(id, name);
    }

    @Override
    public Thermostat createThermostat(String id, String name) {
        return new AppleThermostat(id, name);
    }

    @Override
    public Camera createCamera(String id, String name) {
        return new AppleCamera(id, name);
    }
}
