package com.smarthome.abstractfactory.generic;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.model.Camera;
import com.smarthome.model.Light;
import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.Ecosystem;

/** Family used when the home is not tied to any vendor ecosystem. */
public class GenericHomeFactory implements SmartHomeFactory {

    @Override
    public Ecosystem getEcosystem() {
        return Ecosystem.GENERIC;
    }

    @Override
    public Light createLight(String id, String name) {
        return new Light(id, name, Ecosystem.GENERIC);
    }

    @Override
    public Thermostat createThermostat(String id, String name) {
        return new Thermostat(id, name, Ecosystem.GENERIC);
    }

    @Override
    public Camera createCamera(String id, String name) {
        return new Camera(id, name, Ecosystem.GENERIC);
    }
}
