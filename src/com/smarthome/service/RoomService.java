package com.smarthome.service;

import com.smarthome.exception.RoomNotFoundException;
import com.smarthome.model.Device;
import com.smarthome.model.Room;
import com.smarthome.model.enums.RoomType;
import com.smarthome.singleton.SmartHomeManager;
import com.smarthome.util.IdGenerator;

import java.util.List;

public class RoomService {

    private final SmartHomeManager manager;

    public RoomService(SmartHomeManager manager) {
        this.manager = manager;
    }

    public Room createRoom(String name, RoomType type) {
        Room room = new Room(IdGenerator.next("room"), name, type);
        manager.getHome().addRoom(room);
        return room;
    }

    public Room getRoom(String roomId) {
        return manager.getHome().findRoomById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    public List<Room> listRooms() {
        return manager.getHome().getRooms();
    }

    public Room updateRoom(String roomId, String name, RoomType type) {
        Room room = getRoom(roomId);
        if (name != null) {
            room.setName(name);
        }
        if (type != null) {
            room.setType(type);
        }
        return room;
    }

    /** Removes the room together with the devices it contains. */
    public Room deleteRoom(String roomId) {
        return manager.getHome().removeRoom(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    public List<Device> listDevices(String roomId) {
        return getRoom(roomId).getDevices();
    }
}
