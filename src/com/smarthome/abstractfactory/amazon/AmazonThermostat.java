package com.smarthome.abstractfactory.amazon;

import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.Ecosystem;

public class AmazonThermostat extends Thermostat {

    public AmazonThermostat(String id, String name) {
        super(id, name, Ecosystem.AMAZON);
    }

    @Override
    public String getModel() {
        return "Amazon Home Thermostat";
    }
}
