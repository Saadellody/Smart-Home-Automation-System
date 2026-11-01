package com.smarthome.abstractfactory.google;

import com.smarthome.model.Camera;
import com.smarthome.model.enums.Ecosystem;

public class GoogleCamera extends Camera {

    public GoogleCamera(String id, String name) {
        super(id, name, Ecosystem.GOOGLE);
    }

    @Override
    public String getModel() {
        return "Google Home Camera";
    }
}
