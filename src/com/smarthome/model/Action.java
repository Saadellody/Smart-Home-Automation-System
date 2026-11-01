package com.smarthome.model;

import com.smarthome.exception.InvalidDeviceOperationException;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceType;

import java.util.List;

/**
 * One step of a scenario. Immutable, so clones of a scenario can safely share it.
 * <p>
 * Targets either one device ({@code deviceId}) or every device of a type ({@code deviceType}),
 * optionally restricted to a room ({@code roomId}). Examples: "dev-3 -> TURN_OFF",
 * "every LIGHT -> TURN_OFF", "every LIGHT in room-1 -> SET_BRIGHTNESS 20".
 */
public record Action(ActionType type, String deviceId, DeviceType deviceType, String roomId, Double value) {

    public Action {
        if (type == null) {
            throw new IllegalArgumentException("Action type is required");
        }
        if ((deviceId == null) == (deviceType == null)) {
            throw new IllegalArgumentException("An action targets either a deviceId or a deviceType (exactly one)");
        }
        if (type.requiresValue() && value == null) {
            throw new IllegalArgumentException("Action " + type + " requires a value");
        }
        if (deviceType != null && type.getSupportedType() != null && type.getSupportedType() != deviceType) {
            throw new IllegalArgumentException("Action " + type + " cannot be applied to " + deviceType);
        }
    }

    public static Action forDevice(String deviceId, ActionType type, Double value) {
        return new Action(type, deviceId, null, null, value);
    }

    public static Action forDevice(String deviceId, ActionType type) {
        return forDevice(deviceId, type, null);
    }

    public static Action forAll(DeviceType deviceType, ActionType type, Double value) {
        return new Action(type, null, deviceType, null, value);
    }

    public static Action forAll(DeviceType deviceType, ActionType type) {
        return forAll(deviceType, type, null);
    }

    public static Action forAllInRoom(String roomId, DeviceType deviceType, ActionType type, Double value) {
        return new Action(type, null, deviceType, roomId, value);
    }

    /** Devices of the home this action applies to (may be empty). */
    public List<Device> resolveTargets(SmartHome home) {
        if (deviceId != null) {
            return home.findDeviceById(deviceId).stream().toList();
        }
        return home.getDevices().stream()
                .filter(d -> d.getType() == deviceType)
                .filter(d -> roomId == null || (d.getRoom() != null && roomId.equals(d.getRoom().getId())))
                .toList();
    }

    public void applyTo(Device device) {
        if (!type.supports(device)) {
            throw new InvalidDeviceOperationException(
                    "Action " + type + " is not supported by " + device.getType() + " " + device.getId());
        }
        switch (type) {
            case TURN_ON -> device.turnOn();
            case TURN_OFF -> device.turnOff();
            case SET_BRIGHTNESS -> ((Light) device).setBrightness(intValue());
            case SET_TEMPERATURE -> ((Thermostat) device).setTargetTemperature(value);
            case START_RECORDING -> ((Camera) device).startRecording();
            case STOP_RECORDING -> ((Camera) device).stopRecording();
            case SET_SPEED -> ((Fan) device).setSpeed(intValue());
            case SET_CHANNEL -> ((TV) device).setChannel(intValue());
            case SET_VOLUME -> ((TV) device).setVolume(intValue());
            case LOCK -> ((DoorLock) device).lock();
            case UNLOCK -> ((DoorLock) device).unlock();
        }
    }

    private int intValue() {
        if (value != Math.rint(value)) {
            throw new InvalidDeviceOperationException("Action " + type + " requires a whole number");
        }
        return value.intValue();
    }

    public String describe() {
        String target = deviceId != null ? deviceId : "all " + deviceType + (roomId != null ? " in " + roomId : "");
        return target + " -> " + type + (value != null ? " " + value : "");
    }
}
