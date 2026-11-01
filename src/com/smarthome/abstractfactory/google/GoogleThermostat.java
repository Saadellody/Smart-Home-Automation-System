package com.smarthome.abstractfactory.google;

import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.Ecosystem;

public class GoogleThermostat extends Thermostat {

    public GoogleThermostat(String id, String name) {
        super(id, name, Ecosystem.GOOGLE);
    }

    @Override
    public String getModel() {
        return "Google Home Thermostat";
    }
}
