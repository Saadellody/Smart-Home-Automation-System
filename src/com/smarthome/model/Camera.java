package com.smarthome.model;

import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;

public class Camera extends Device {

    private boolean recording;

    public Camera(String id, String name, Ecosystem ecosystem) {
        super(id, name, ecosystem);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.CAMERA;
    }

    public void startRecording() {
        turnOn();
        recording = true;
    }

    public void stopRecording() {
        recording = false;
    }

    @Override
    public void turnOff() {
        super.turnOff();
        recording = false;
    }

    public boolean isRecording() {
        return recording;
    }
}
