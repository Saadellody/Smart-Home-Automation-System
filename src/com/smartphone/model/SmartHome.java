package com.smartphone.model;

import com.smartphone.model.Enum.RoomType;

import java.util.List;

public class SmartHome {

    private Long id;
    private String type;
    private String name;
    private List<Room> rooms;
    private List<Device> devices;
    private AutomationScenario automationScenario;
    private HomeConfiguration homeConfiguration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public AutomationScenario getAutomationScenario() {
        return automationScenario;
    }

    public void setAutomationScenario(AutomationScenario automationScenario) {
        this.automationScenario = automationScenario;
    }

    public HomeConfiguration getHomeConfiguration() {
        return homeConfiguration;
    }

    public void setHomeConfiguration(HomeConfiguration homeConfiguration) {
        this.homeConfiguration = homeConfiguration;
    }

    public int addRoom(Room room){
        if(room== null){
            return -1;
        }else if (rooms.contains(room)){
            return -2;
        }else{
            rooms.add(room);
            return 1;
        }
    }

    public int removeRoom(String roomId){
        if(roomId==null){
            return -1;
        }else if (rooms.isEmpty()){
            return -2 ;
        }else if (rooms.stream().noneMatch(room -> roomId.equals(room.getId()))){
        return -3;
        }else{
            rooms.remove(roomId);
            return 1;
        }
    }

    public Room findRoomBYId(String RoomID){
        if (RoomID == null || RoomID.isEmpty()) {
            return null;
        }

        return rooms.stream()
                .filter(room -> RoomID.equals(room.getId()))
                .findFirst()
                .orElse(null);

    }
    public List<Room> getAllRooms() {
        return rooms;
    }
}
