package com.smarthome.abstractfactory.apple;

import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.Ecosystem;

public class AppleThermostat extends Thermostat {

    public AppleThermostat(String id, String name) {
        super(id, name, Ecosystem.APPLE);
    }

    @Override
    public String getModel() {
        return "Apple Home Thermostat";
    }
}
