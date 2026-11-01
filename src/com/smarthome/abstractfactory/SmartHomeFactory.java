package com.smarthome.abstractfactory;

import com.smarthome.abstractfactory.amazon.AmazonHomeFactory;
import com.smarthome.abstractfactory.apple.AppleHomeFactory;
import com.smarthome.abstractfactory.generic.GenericHomeFactory;
import com.smarthome.abstractfactory.google.GoogleHomeFactory;
import com.smarthome.model.Camera;
import com.smarthome.model.Light;
import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.Ecosystem;

/**
 * Abstract Factory: creates a family of devices that all belong to the same ecosystem,
 * so a Google home never ends up with an Apple thermostat by accident.
 */
public interface SmartHomeFactory {

    Ecosystem getEcosystem();

    Light createLight(String id, String name);

    Thermostat createThermostat(String id, String name);

    Camera createCamera(String id, String name);

    static SmartHomeFactory forEcosystem(Ecosystem ecosystem) {
        return switch (ecosystem == null ? Ecosystem.GENERIC : ecosystem) {
            case GOOGLE -> new GoogleHomeFactory();
            case APPLE -> new AppleHomeFactory();
            case AMAZON -> new AmazonHomeFactory();
            case GENERIC -> new GenericHomeFactory();
        };
    }
}
