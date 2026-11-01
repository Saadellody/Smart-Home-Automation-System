package com.smarthome.api;

import com.smarthome.model.Action;
import com.smarthome.model.AutomationScenario;
import com.smarthome.model.Camera;
import com.smarthome.model.Device;
import com.smarthome.model.DoorLock;
import com.smarthome.model.ExecutionReport;
import com.smarthome.model.Fan;
import com.smarthome.model.HomeConfiguration;
import com.smarthome.model.Light;
import com.smarthome.model.Room;
import com.smarthome.model.SmartHome;
import com.smarthome.model.TV;
import com.smarthome.model.Thermostat;
import com.smarthome.model.Trigger;
import com.smarthome.model.Notification;
import com.smarthome.model.enums.ActionType;
import com.smarthome.singleton.SmartHomeManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Converts domain objects to JSON-ready maps (keeps the domain free of any serialization concern). */
public final class JsonMapper {

    private JsonMapper() {
    }

    public static Map<String, Object> home(SmartHome home) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", home.getId());
        json.put("name", home.getName());
        json.put("ecosystem", home.getEcosystem());
        json.put("activeScenarioId", home.getActiveScenarioId());
        json.put("securityArmed", home.isSecurityArmed());
        json.put("roomCount", home.getRooms().size());
        json.put("deviceCount", home.getDevices().size());
        json.put("devicesOn", home.getDevices().stream().filter(Device::isOn).count());
        json.put("scenarioCount", home.getScenarios().size());
        json.put("configuration", configuration(home.getConfiguration()));
        json.put("rooms", home.getRooms().stream().map(JsonMapper::room).toList());
        return json;
    }

    public static Map<String, Object> configuration(HomeConfiguration config) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("temperatureUnit", config.getTemperatureUnit());
        json.put("defaultTemperature", config.fromCelsius(config.getDefaultTemperature()));
        json.put("securityEnabled", config.isSecurityEnabled());
        json.put("notificationsEnabled", config.isNotificationsEnabled());
        json.put("autoModeEnabled", config.isAutoModeEnabled());
        return json;
    }

    public static Map<String, Object> room(Room room) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", room.getId());
        json.put("name", room.getName());
        json.put("type", room.getType());
        json.put("deviceCount", room.getDevices().size());
        json.put("devices", devices(room.getDevices()));
        return json;
    }

    public static List<Map<String, Object>> rooms(List<Room> rooms) {
        return rooms.stream().map(JsonMapper::room).toList();
    }

    public static Map<String, Object> device(Device device) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", device.getId());
        json.put("name", device.getName());
        json.put("type", device.getType());
        json.put("model", device.getModel());
        json.put("ecosystem", device.getEcosystem());
        json.put("status", device.getStatus());
        json.put("roomId", device.getRoom() == null ? null : device.getRoom().getId());
        json.put("roomName", device.getRoom() == null ? null : device.getRoom().getName());
        switch (device) {
            case Light l -> json.put("brightness", l.getBrightness());
            case Thermostat t -> {
                json.put("targetTemperature", config().fromCelsius(t.getTargetTemperature()));
                json.put("temperatureUnit", config().getTemperatureUnit());
            }
            case Camera c -> json.put("recording", c.isRecording());
            case Fan f -> json.put("speed", f.getSpeed());
            case TV tv -> {
                json.put("channel", tv.getChannel());
                json.put("volume", tv.getVolume());
            }
            case DoorLock lock -> json.put("locked", lock.isLocked());
            default -> {
            }
        }
        return json;
    }

    public static List<Map<String, Object>> devices(List<Device> devices) {
        return devices.stream().map(JsonMapper::device).toList();
    }

    public static Map<String, Object> scenario(AutomationScenario scenario) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", scenario.getId());
        json.put("name", scenario.getName());
        json.put("description", scenario.getDescription());
        json.put("enabled", scenario.isEnabled());
        json.put("securityMode", scenario.getSecurityAction());
        json.put("notifyOnExecution", scenario.isNotifyOnExecution());
        json.put("notificationMessage", scenario.getNotificationMessage());
        json.put("trigger", trigger(scenario.getTrigger()));
        json.put("actions", scenario.getActions().stream().map(JsonMapper::action).toList());
        return json;
    }

    public static List<Map<String, Object>> scenarios(List<AutomationScenario> scenarios) {
        return scenarios.stream().map(JsonMapper::scenario).toList();
    }

    public static Map<String, Object> trigger(Trigger trigger) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", trigger.type());
        json.put("value", trigger.value());
        return json;
    }

    public static Map<String, Object> action(Action action) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", action.type());
        json.put("deviceId", action.deviceId());
        json.put("deviceType", action.deviceType());
        json.put("roomId", action.roomId());
        Double value = action.value();
        if (action.type() == ActionType.SET_TEMPERATURE && value != null) {
            value = config().fromCelsius(value);
        }
        json.put("value", value);
        return json;
    }

    /**
     * Converts a value received from a client to the domain's unit: temperatures arrive in the
     * configured unit and are stored in Celsius; every other value is unchanged.
     */
    public static Double toDomainValue(ActionType type, Double value) {
        if (type != ActionType.SET_TEMPERATURE || value == null) {
            return value;
        }
        return config().toCelsius(value);
    }

    /** The global configuration (temperature unit), held by the Singleton. */
    private static HomeConfiguration config() {
        return SmartHomeManager.getInstance().getConfiguration();
    }

    public static Map<String, Object> notification(Notification notification) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", notification.id());
        json.put("time", notification.time().toString());
        json.put("type", notification.type());
        json.put("message", notification.message());
        return json;
    }

    public static List<Map<String, Object>> templates(Map<String, AutomationScenario> templates) {
        return templates.entrySet().stream().map(e -> {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("key", e.getKey());
            json.putAll(scenario(e.getValue()));
            json.remove("id");
            return json;
        }).toList();
    }

    public static Map<String, Object> report(ExecutionReport report) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("scenarioId", report.getScenarioId());
        json.put("scenarioName", report.getScenarioName());
        json.put("applied", report.count(ExecutionReport.Outcome.APPLIED));
        json.put("skipped", report.count(ExecutionReport.Outcome.SKIPPED));
        json.put("failed", report.count(ExecutionReport.Outcome.FAILED));
        json.put("entries", report.getEntries().stream().map(e -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("action", e.action());
            entry.put("deviceId", e.deviceId());
            entry.put("deviceName", e.deviceName());
            entry.put("outcome", e.outcome());
            entry.put("message", e.message());
            return entry;
        }).toList());
        return json;
    }
}
