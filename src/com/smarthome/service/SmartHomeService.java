package com.smarthome.service;

import com.smarthome.model.Device;
import com.smarthome.model.HomeConfiguration;
import com.smarthome.model.Room;
import com.smarthome.model.SmartHome;
import com.smarthome.model.enums.Ecosystem;
import com.smarthome.model.enums.TemperatureUnit;
import com.smarthome.singleton.SmartHomeManager;

/** Home-level operations: creating the home, choosing its ecosystem, configuration and global state. */
public class SmartHomeService {

    private final SmartHomeManager manager;

    public SmartHomeService(SmartHomeManager manager) {
        this.manager = manager;
    }

    public SmartHome getHome() {
        return manager.getHome();
    }

    /** Creates a brand-new empty home, replacing the current one. */
    public SmartHome createHome(String name, Ecosystem ecosystem) {
        return manager.createHome(name, ecosystem);
    }

    /** Renames the home and/or changes its ecosystem. A new ecosystem only applies to devices created afterwards. */
    public SmartHome updateHome(String name, Ecosystem ecosystem) {
        SmartHome home = manager.getHome();
        if (name != null) {
            home.setName(name);
        }
        if (ecosystem != null) {
            home.setEcosystem(ecosystem);
        }
        return home;
    }

    public HomeConfiguration getConfiguration() {
        return manager.getConfiguration();
    }

    /** Updates the configuration; null arguments leave the current value unchanged. */
    public HomeConfiguration updateConfiguration(TemperatureUnit unit, Double defaultTemperature,
                                                 Boolean securityEnabled, Boolean notificationsEnabled,
                                                 Boolean autoModeEnabled) {
        HomeConfiguration config = manager.getConfiguration();
        // Validated first so an invalid request leaves the configuration untouched.
        if (defaultTemperature != null) {
            config.setDefaultTemperature(defaultTemperature);
        }
        if (unit != null) {
            config.setTemperatureUnit(unit);
        }
        if (securityEnabled != null) {
            config.setSecurityEnabled(securityEnabled);
        }
        if (notificationsEnabled != null) {
            config.setNotificationsEnabled(notificationsEnabled);
        }
        if (autoModeEnabled != null) {
            config.setAutoModeEnabled(autoModeEnabled);
        }
        return config;
    }

    /** Human-readable snapshot of the whole home, used by the CLI. */
    public String describeState() {
        SmartHome home = manager.getHome();
        StringBuilder sb = new StringBuilder();
        sb.append("Home '").append(home.getName()).append("' (").append(home.getEcosystem()).append(")\n");
        for (Room room : home.getRooms()) {
            sb.append("  ").append(room.getName()).append(" [").append(room.getType()).append("]\n");
            for (Device device : room.getDevices()) {
                sb.append("    - ").append(device.getName())
                        .append(" (").append(device.getModel()).append("): ")
                        .append(DeviceService.describeState(device)).append('\n');
            }
        }
        sb.append("  Scenarios: ").append(home.getScenarios().size());
        if (home.getActiveScenarioId() != null) {
            sb.append(", last executed: ").append(home.getActiveScenarioId());
        }
        return sb.toString();
    }
}
