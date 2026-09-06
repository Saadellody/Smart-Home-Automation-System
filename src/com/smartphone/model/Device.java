package com.smartphone.model;

import com.smartphone.model.Enum.DeviceStatus;

public class Device {

    private String id ;
    private String name;
    private DeviceStatus status;
    private Room room;

    public Device(String id, String name, DeviceStatus status, Room room) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.room = room;
    }

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

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
//    4. Device methods
//
//    Our current attributes:
//
//    Device
//├── id : String
//├── name : String
//├── status : DeviceStatus
//└── room : Room
//
//    Now methods.
//
//    Basic device operations
//
//    Every device should probably have:
//
//    turnOn()
//    turnOff()
//
//    But there is an important point here:
//
//    Don't assume every device behaves identically.
//
//    For example:
//
//    Light → turn on/off
//    TV → turn on/off
//    Camera → start/stop recording
//    DoorLock → lock/unlock
//    Thermostat → set temperature
//
//    So Device can contain common behavior, while subclasses add their own specific behavior.
//
//    Status
//    getStatus()
//    Identification
//    getId()
//    getName()
//    Rename
//    setName(String name)
//    Room
//
//    Since a device belongs to a room:
//
//    getRoom()
//
//    Potentially:
//
//    setRoom(Room room)
//
//    Again, I'd be careful about making this publicly modifiable. The relationship should ideally be maintained through the room/home management logic.
//
//            5. Important distinction
//
//    Don't put these methods into Device:
//
//    setTemperature()
//    startRecording()
//    lock()
//    unlock()
//    changeBrightness()
//    changeChannel()
//
//    Because not every device has those capabilities.    4. Device methods
//
//    Our current attributes:
//
//    Device
//├── id : String
//├── name : String
//├── status : DeviceStatus
//└── room : Room
//
//    Now methods.
//
//    Basic device operations
//
//    Every device should probably have:
//
//    turnOn()
//    turnOff()
//
//    But there is an important point here:
//
//    Don't assume every device behaves identically.
//
//    For example:
//
//    Light → turn on/off
//    TV → turn on/off
//    Camera → start/stop recording
//    DoorLock → lock/unlock
//    Thermostat → set temperature
//
//    So Device can contain common behavior, while subclasses add their own specific behavior.
//
//    Status
//    getStatus()
//    Identification
//    getId()
//    getName()
//    Rename
//    setName(String name)
//    Room
//
//    Since a device belongs to a room:
//
//    getRoom()
//
//    Potentially:
//
//    setRoom(Room room)
//
//    Again, I'd be careful about making this publicly modifiable. The relationship should ideally be maintained through the room/home management logic.
//
//            5. Important distinction
//
//    Don't put these methods into Device:
//
//    setTemperature()
//    startRecording()
//    lock()
//    unlock()
//    changeBrightness()
//    changeChannel()
//
//    Because not every device has those capabilities.
}
