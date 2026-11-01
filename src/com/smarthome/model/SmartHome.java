package com.smarthome.model;

import com.smarthome.model.enums.Ecosystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** The whole house: its rooms (which own the devices), its automation scenarios and its configuration. */
public class SmartHome {

    private final String id;
    private String name;
    private Ecosystem ecosystem;
    private final List<Room> rooms = new ArrayList<>();
    private final List<AutomationScenario> scenarios = new ArrayList<>();
    private String activeScenarioId;
    private final HomeConfiguration configuration = new HomeConfiguration();

    public SmartHome(String id, String name, Ecosystem ecosystem) {
        this.id = id;
        setName(name);
        setEcosystem(ecosystem);
    }

    // ---- rooms ----

    public void addRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room is required");
        }
        if (findRoomById(room.getId()).isPresent()) {
            throw new IllegalArgumentException("A room with id " + room.getId() + " already exists");
        }
        rooms.add(room);
        room.setHome(this);
    }

    public Optional<Room> removeRoom(String roomId) {
        Optional<Room> room = findRoomById(roomId);
        room.ifPresent(r -> {
            rooms.remove(r);
            r.setHome(null);
        });
        return room;
    }

    public Optional<Room> findRoomById(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return Optional.empty();
        }
        return rooms.stream().filter(room -> roomId.equals(room.getId())).findFirst();
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    // ---- devices (owned by rooms) ----

    public List<Device> getDevices() {
        return rooms.stream().flatMap(room -> room.getDevices().stream()).toList();
    }

    public Optional<Device> findDeviceById(String deviceId) {
        return rooms.stream()
                .map(room -> room.findDeviceById(deviceId))
                .flatMap(Optional::stream)
                .findFirst();
    }

    // ---- automation scenarios ----

    public void addScenario(AutomationScenario scenario) {
        if (scenario == null) {
            throw new IllegalArgumentException("Scenario is required");
        }
        if (findScenarioById(scenario.getId()).isPresent()) {
            throw new IllegalArgumentException("A scenario with id " + scenario.getId() + " already exists");
        }
        scenarios.add(scenario);
    }

    public Optional<AutomationScenario> removeScenario(String scenarioId) {
        Optional<AutomationScenario> scenario = findScenarioById(scenarioId);
        scenario.ifPresent(s -> {
            scenarios.remove(s);
            if (s.getId().equals(activeScenarioId)) {
                activeScenarioId = null;
            }
        });
        return scenario;
    }

    public Optional<AutomationScenario> findScenarioById(String scenarioId) {
        if (scenarioId == null) {
            return Optional.empty();
        }
        return scenarios.stream().filter(s -> scenarioId.equals(s.getId())).findFirst();
    }

    public List<AutomationScenario> getScenarios() {
        return Collections.unmodifiableList(scenarios);
    }

    /** Id of the last executed scenario, or null. */
    public String getActiveScenarioId() {
        return activeScenarioId;
    }

    public void setActiveScenarioId(String activeScenarioId) {
        this.activeScenarioId = activeScenarioId;
    }

    // ---- attributes ----

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Home name is required");
        }
        this.name = name.trim();
    }

    public Ecosystem getEcosystem() {
        return ecosystem;
    }

    public void setEcosystem(Ecosystem ecosystem) {
        this.ecosystem = ecosystem == null ? Ecosystem.GENERIC : ecosystem;
    }

    public HomeConfiguration getConfiguration() {
        return configuration;
    }
}
