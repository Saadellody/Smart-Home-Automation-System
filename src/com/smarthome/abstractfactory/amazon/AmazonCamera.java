package com.smarthome.abstractfactory.amazon;

import com.smarthome.model.Camera;
import com.smarthome.model.enums.Ecosystem;

public class AmazonCamera extends Camera {

    public AmazonCamera(String id, String name) {
        super(id, name, Ecosystem.AMAZON);
    }

    @Override
    public String getModel() {
        return "Amazon Home Camera";
    }
}
