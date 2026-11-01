package com.smarthome.abstractfactory.google;

import com.smarthome.model.Light;
import com.smarthome.model.enums.Ecosystem;

public class GoogleLight extends Light {

    public GoogleLight(String id, String name) {
        super(id, name, Ecosystem.GOOGLE);
    }

    @Override
    public String getModel() {
        return "Google Home Light";
    }
}
