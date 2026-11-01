package com.smarthome.service;

import com.smarthome.exception.DeviceNotFoundException;
import com.smarthome.exception.SecurityModeException;
import com.smarthome.model.HomeConfiguration;
import com.smarthome.model.enums.NotificationType;
import com.smarthome.factory.DeviceCreator;
import com.smarthome.model.Action;
import com.smarthome.model.Camera;
import com.smarthome.model.Device;
import com.smarthome.model.DoorLock;
import com.smarthome.model.Fan;
import com.smarthome.model.Light;
import com.smarthome.model.Room;
import com.smarthome.model.TV;
import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceStatus;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.singleton.SmartHomeManager;

import java.util.List;

public class DeviceService {

    private final SmartHomeManager manager;
    private final RoomService roomService;
    private final NotificationService notifications;

    public DeviceService(SmartHomeManager manager, RoomService roomService, NotificationService notifications) {
        this.manager = manager;
        this.roomService = roomService;
        this.notifications = notifications;
    }

    /**
     * Creates a device of the given type in a room. The concrete class is chosen by the
     * Factory Method creators, using the home's ecosystem family (Abstract Factory).
     */
    public Device addDevice(String roomId, DeviceType type, String name) {
        Room room = roomService.getRoom(roomId);
        Device device = DeviceCreator.forType(type).create(name, manager.getDeviceFamily());
        if (device instanceof Thermostat thermostat) {
            thermostat.setTargetTemperature(manager.getConfiguration().getDefaultTemperature());
            thermostat.turnOff();
        }
        room.addDevice(device);
        return device;
    }

    public Device getDevice(String deviceId) {
        return manager.getHome().findDeviceById(deviceId).orElseThrow(() -> new DeviceNotFoundException(deviceId));
    }

    /** Lists devices; every filter is optional (null = no filter). */
    public List<Device> listDevices(DeviceType type, String roomId, DeviceStatus status) {
        if (roomId != null) {
            roomService.getRoom(roomId);
        }
        return manager.getHome().getDevices().stream()
                .filter(d -> type == null || d.getType() == type)
                .filter(d -> roomId == null || roomId.equals(d.getRoom().getId()))
                .filter(d -> status == null || d.getStatus() == status)
                .toList();
    }

    public Device renameDevice(String deviceId, String name) {
        Device device = getDevice(deviceId);
        device.setName(name);
        return device;
    }

    public Device moveDevice(String deviceId, String roomId) {
        Device device = getDevice(deviceId);
        roomService.getRoom(roomId).addDevice(device);
        return device;
    }

    public Device removeDevice(String deviceId) {
        Device device = getDevice(deviceId);
        device.getRoom().removeDevice(deviceId);
        return device;
    }

    public Device turnOn(String deviceId) {
        return executeCommand(deviceId, ActionType.TURN_ON, null);
    }

    public Device turnOff(String deviceId) {
        return executeCommand(deviceId, ActionType.TURN_OFF, null);
    }

    /**
     * Runs any device-specific command, e.g. SET_BRIGHTNESS 40 on a light or LOCK on a door lock.
     * Temperatures are in Celsius; callers convert from the configured unit first.
     */
    public Device executeCommand(String deviceId, ActionType type, Double value) {
        Device device = getDevice(deviceId);
        try {
            Action.forDevice(deviceId, type, value).applyTo(device);
        } catch (SecurityModeException e) {
            notifications.notify(NotificationType.SECURITY, "Blocked " + type + " on '" + device.getName() + "'");
            throw e;
        }
        return device;
    }

    /** Short text describing a device's current state, e.g. "ON, brightness 40%". */
    public static String describeState(Device device, HomeConfiguration config) {
        String details = switch (device) {
            case Light l -> "brightness " + l.getBrightness() + "%";
            case Thermostat t -> "target " + config.fromCelsius(t.getTargetTemperature())
                    + " " + config.getTemperatureUnit().getSymbol();
            case Camera c -> c.isRecording() ? "recording" : "not recording";
            case Fan f -> "speed " + f.getSpeed();
            case TV tv -> "channel " + tv.getChannel() + ", volume " + tv.getVolume();
            case DoorLock lock -> lock.isLocked() ? "locked" : "unlocked";
            default -> "";
        };
        return device.getStatus() + (details.isEmpty() ? "" : ", " + details);
    }
}
