package com.smarthome.abstractfactory.apple;

import com.smarthome.model.Light;
import com.smarthome.model.enums.Ecosystem;

public class AppleLight extends Light {

    public AppleLight(String id, String name) {
        super(id, name, Ecosystem.APPLE);
    }

    @Override
    public String getModel() {
        return "Apple Home Light";
    }
}
