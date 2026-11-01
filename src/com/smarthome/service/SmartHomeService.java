package com.smarthome.service;

import com.smarthome.model.Device;
import com.smarthome.model.HomeConfiguration;
import com.smarthome.model.Room;
import com.smarthome.model.SmartHome;
import com.smarthome.model.enums.Ecosystem;
import com.smarthome.model.enums.NotificationType;
import com.smarthome.model.enums.TemperatureUnit;
import com.smarthome.singleton.SmartHomeManager;

/** Home-level operations: creating the home, choosing its ecosystem, configuration, security and global state. */
public class SmartHomeService {

    private final SmartHomeManager manager;
    private final NotificationService notifications;

    public SmartHomeService(SmartHomeManager manager, NotificationService notifications) {
        this.manager = manager;
        this.notifications = notifications;
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

    /**
     * Updates the configuration; null arguments leave the current value unchanged.
     * {@code defaultTemperature} is expressed in the unit given in the same call, or else the current unit.
     */
    public HomeConfiguration updateConfiguration(TemperatureUnit unit, Double defaultTemperature,
                                                 Boolean securityEnabled, Boolean notificationsEnabled,
                                                 Boolean autoModeEnabled) {
        HomeConfiguration config = manager.getConfiguration();
        // Validated first so an invalid request leaves the configuration untouched.
        if (defaultTemperature != null) {
            TemperatureUnit inputUnit = unit != null ? unit : config.getTemperatureUnit();
            config.setDefaultTemperature(inputUnit.toCelsius(defaultTemperature));
        }
        if (unit != null) {
            config.setTemperatureUnit(unit);
        }
        if (notificationsEnabled != null) {
            config.setNotificationsEnabled(notificationsEnabled);
        }
        if (autoModeEnabled != null) {
            config.setAutoModeEnabled(autoModeEnabled);
        }
        if (securityEnabled != null) {
            if (securityEnabled) {
                armSecurity();
            } else {
                disarmSecurity();
            }
        }
        return config;
    }

    /** Locks every door, starts every camera and refuses actions that would weaken protection. */
    public SmartHome armSecurity() {
        SmartHome home = manager.getHome();
        if (!home.isSecurityArmed()) {
            home.armSecurity();
            notifications.notify(NotificationType.SECURITY, "Security armed: doors locked, cameras recording");
        }
        return home;
    }

    public SmartHome disarmSecurity() {
        SmartHome home = manager.getHome();
        if (home.isSecurityArmed()) {
            home.disarmSecurity();
            notifications.notify(NotificationType.SECURITY, "Security disarmed");
        }
        return home;
    }

    /** Human-readable snapshot of the whole home, used by the CLI. */
    public String describeState() {
        SmartHome home = manager.getHome();
        HomeConfiguration config = home.getConfiguration();
        StringBuilder sb = new StringBuilder();
        sb.append("Home '").append(home.getName()).append("' (").append(home.getEcosystem()).append(")")
                .append("  security ").append(home.isSecurityArmed() ? "ARMED" : "disarmed")
                .append(", auto mode ").append(config.isAutoModeEnabled() ? "on" : "off")
                .append(", notifications ").append(config.isNotificationsEnabled() ? "on" : "off")
                .append(", unit ").append(config.getTemperatureUnit().getSymbol()).append('\n');
        for (Room room : home.getRooms()) {
            sb.append("  ").append(room.getName()).append(" [").append(room.getId()).append(", ")
                    .append(room.getType()).append("]\n");
            for (Device device : room.getDevices()) {
                sb.append("    - ").append(device.getId()).append("  ").append(device.getName())
                        .append(" (").append(device.getModel()).append("): ")
                        .append(DeviceService.describeState(device, config)).append('\n');
            }
        }
        sb.append("  Scenarios: ").append(home.getScenarios().size());
        if (home.getActiveScenarioId() != null) {
            sb.append(", last executed: ").append(home.getActiveScenarioId());
        }
        return sb.toString();
    }
}
