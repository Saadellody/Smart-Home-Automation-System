package com.smarthome.abstractfactory.google;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Camera;
import com.smarthome.model.Light;
import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.Ecosystem;

/** Creates the compatible family of Google devices. */
public class GoogleHomeFactory implements SmartHomeFactory {

    @Override
    public Ecosystem getEcosystem() {
        return Ecosystem.GOOGLE;
    }

    @Override
    public Light createLight(String id, String name) {
        return new GoogleLight(id, name);
    }

    @Override
    public Thermostat createThermostat(String id, String name) {
        return new GoogleThermostat(id, name);
    }

    @Override
    public Camera createCamera(String id, String name) {
        return new GoogleCamera(id, name);
    }
}
