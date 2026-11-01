package com.smarthome.model;

import com.smarthome.model.enums.RoomType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Room {

    private final String id;
    private String name;
    private RoomType type;
    private final List<Device> devices = new ArrayList<>();
    private SmartHome home;

    public Room(String id, String name, RoomType type) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Room id is required");
        }
        this.id = id;
        setName(name);
        setType(type);
    }

    /** Adds the device to this room, moving it out of its previous room if needed. */
    public void addDevice(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device is required");
        }
        if (devices.contains(device)) {
            return;
        }
        if (device.getRoom() != null) {
            device.getRoom().removeDevice(device.getId());
        }
        devices.add(device);
        device.setRoom(this);
    }

    public Optional<Device> removeDevice(String deviceId) {
        Optional<Device> device = findDeviceById(deviceId);
        device.ifPresent(d -> {
            devices.remove(d);
            d.setRoom(null);
        });
        return device;
    }

    public Optional<Device> findDeviceById(String deviceId) {
        if (deviceId == null) {
            return Optional.empty();
        }
        return devices.stream().filter(d -> deviceId.equals(d.getId())).findFirst();
    }

    public List<Device> getDevices() {
        return Collections.unmodifiableList(devices);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name is required");
        }
        this.name = name.trim();
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        if (type == null) {
            throw new IllegalArgumentException("Room type is required");
        }
        this.type = type;
    }

    public SmartHome getHome() {
        return home;
    }

    /** Maintained by {@link SmartHome#addRoom(Room)} and {@link SmartHome#removeRoom(String)}. */
    void setHome(SmartHome home) {
        this.home = home;
    }
}
