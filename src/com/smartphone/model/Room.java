package com.smartphone.model;

import com.smartphone.model.Enum.RoomType;

import java.util.List;

public class Room {

    private String id;
    private String name;
    private RoomType type;
    private List<Device> devices;
    private SmartHome smartHome;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public SmartHome getSmartHome() {
        return smartHome;
    }

    public void setSmartHome(SmartHome smartHome) {
        this.smartHome = smartHome;
    }

    //    Room management
//    Add device
//    addDevice(Device device)
//
//    A room can contain multiple devices.
//
//    Remove device
//    removeDevice(String deviceId)
//    Find device
//    findDeviceById(String deviceId)
//    Get devices
//    getDevices()
//    Get home
//
//    Since Room has:
//
//    home : SmartHome
//
//    you need:
//
//    getHome()
//    Set home
//    setHome(SmartHome home)
}
