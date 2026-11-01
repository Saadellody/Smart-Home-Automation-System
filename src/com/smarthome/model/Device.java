package com.smarthome.model;

import com.smarthome.model.enums.DeviceStatus;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;

/**
 * Common behaviour of every smart device: identity, power status, room and ecosystem.
 * Capability-specific operations (brightness, temperature, locking...) live in the subclasses.
 */
public abstract class Device {

    private final String id;
    private String name;
    private DeviceStatus status;
    private Room room;
    private final Ecosystem ecosystem;

    protected Device(String id, String name, Ecosystem ecosystem) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Device id is required");
        }
        this.id = id;
        setName(name);
        this.status = DeviceStatus.OFF;
        this.ecosystem = ecosystem == null ? Ecosystem.GENERIC : ecosystem;
    }

    public abstract DeviceType getType();

    /** Commercial model name; ecosystem-specific products override it. */
    public String getModel() {
        return "Generic " + getType().name().replace('_', ' ').toLowerCase();
    }

    public void turnOn() {
        status = DeviceStatus.ON;
    }

    public void turnOff() {
        status = DeviceStatus.OFF;
    }

    public boolean isOn() {
        return status == DeviceStatus.ON;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Device name is required");
        }
        this.name = name.trim();
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public Room getRoom() {
        return room;
    }

    /** The device/room relationship is maintained by {@link Room#addDevice(Device)} only. */
    void setRoom(Room room) {
        this.room = room;
    }

    public Ecosystem getEcosystem() {
        return ecosystem;
    }

    @Override
    public String toString() {
        return getType() + "[" + id + ", " + name + ", " + status + "]";
    }
}
