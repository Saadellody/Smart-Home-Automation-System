package com.smarthome.model.enums;

import com.smarthome.model.Device;

/**
 * Every operation an automation (or a direct device command) can perform.
 * Each action declares which device type supports it (null = every device) and whether it needs a value.
 */
public enum ActionType {
    TURN_ON(null, false),
    TURN_OFF(null, false),
    SET_BRIGHTNESS(DeviceType.LIGHT, true),
    SET_TEMPERATURE(DeviceType.THERMOSTAT, true),
    START_RECORDING(DeviceType.CAMERA, false),
    STOP_RECORDING(DeviceType.CAMERA, false),
    SET_SPEED(DeviceType.FAN, true),
    SET_CHANNEL(DeviceType.TV, true),
    SET_VOLUME(DeviceType.TV, true),
    LOCK(DeviceType.DOOR_LOCK, false),
    UNLOCK(DeviceType.DOOR_LOCK, false);

    private final DeviceType supportedType;
    private final boolean requiresValue;

    ActionType(DeviceType supportedType, boolean requiresValue) {
        this.supportedType = supportedType;
        this.requiresValue = requiresValue;
    }

    public DeviceType getSupportedType() {
        return supportedType;
    }

    public boolean requiresValue() {
        return requiresValue;
    }

    public boolean supports(Device device) {
        return supportedType == null || device.getType() == supportedType;
    }
}
