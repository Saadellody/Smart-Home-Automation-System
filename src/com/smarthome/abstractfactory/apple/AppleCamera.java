package com.smarthome.abstractfactory.apple;

import com.smarthome.model.Camera;
import com.smarthome.model.enums.Ecosystem;

public class AppleCamera extends Camera {

    public AppleCamera(String id, String name) {
        super(id, name, Ecosystem.APPLE);
    }

    @Override
    public String getModel() {
        return "Apple Home Camera";
    }
}
