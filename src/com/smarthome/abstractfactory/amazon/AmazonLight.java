package com.smarthome.abstractfactory.amazon;

import com.smarthome.model.Light;
import com.smarthome.model.enums.Ecosystem;

public class AmazonLight extends Light {

    public AmazonLight(String id, String name) {
        super(id, name, Ecosystem.AMAZON);
    }

    @Override
    public String getModel() {
        return "Amazon Home Light";
    }
}
