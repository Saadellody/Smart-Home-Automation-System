package com.smarthome.abstractfactory.amazon;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Camera;
import com.smarthome.model.Light;
import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.Ecosystem;

/** Creates the compatible family of Amazon devices. */
public class AmazonHomeFactory implements SmartHomeFactory {

    @Override
    public Ecosystem getEcosystem() {
        return Ecosystem.AMAZON;
    }

    @Override
    public Light createLight(String id, String name) {
        return new AmazonLight(id, name);
    }

    @Override
    public Thermostat createThermostat(String id, String name) {
        return new AmazonThermostat(id, name);
    }

    @Override
    public Camera createCamera(String id, String name) {
        return new AmazonCamera(id, name);
    }
}
