package com.smartphone.model;

import com.smartphone.model.Enum.DeviceStatus;

public class Device {

    private Long id ;
    private String name;
    private DeviceStatus status;
    private Room room;


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
