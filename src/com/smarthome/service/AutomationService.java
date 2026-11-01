package com.smarthome.service;

import com.smarthome.builder.AutomationScenarioBuilder;
import com.smarthome.exception.InvalidAutomationException;
import com.smarthome.exception.ScenarioNotFoundException;
import com.smarthome.model.Action;
import com.smarthome.model.AutomationScenario;
import com.smarthome.model.Device;
import com.smarthome.model.ExecutionReport;
import com.smarthome.model.SmartHome;
import com.smarthome.model.Trigger;
import com.smarthome.model.enums.NotificationType;
import com.smarthome.model.enums.SecurityAction;
import com.smarthome.model.enums.TriggerType;
import com.smarthome.prototype.AutomationPrototype;
import com.smarthome.singleton.SmartHomeManager;
import com.smarthome.util.IdGenerator;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AutomationService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final SmartHomeManager manager;
    private final NotificationService notifications;
    private final AutomationPrototype templates;

    public AutomationService(SmartHomeManager manager, NotificationService notifications) {
        this(manager, notifications, AutomationPrototype.withDefaults());
    }

    public AutomationService(SmartHomeManager manager, NotificationService notifications, AutomationPrototype templates) {
        this.manager = manager;
        this.notifications = notifications;
        this.templates = templates;
    }

    public AutomationScenario createScenario(AutomationScenarioBuilder builder) {
        return save(builder.build());
    }

    private AutomationScenario save(AutomationScenario scenario) {
        validate(scenario);
        manager.getHome().addScenario(scenario);
        return scenario;
    }

    /** Checks that every action targets something that exists and supports the action. */
    public void validate(AutomationScenario scenario) {
        validateActions(scenario.getActions());
    }

    private void validateActions(List<Action> actions) {
        if (actions == null || actions.isEmpty()) {
            throw new InvalidAutomationException("A scenario needs at least one action");
        }
        SmartHome home = manager.getHome();
        for (Action action : actions) {
            if (action.deviceId() != null) {
                Optional<Device> device = home.findDeviceById(action.deviceId());
                if (device.isEmpty()) {
                    throw new InvalidAutomationException("Action targets unknown device " + action.deviceId());
                }
                if (!action.type().supports(device.get())) {
                    throw new InvalidAutomationException("Action " + action.type() + " is not supported by "
                            + device.get().getType() + " " + action.deviceId());
                }
            }
            if (action.roomId() != null && home.findRoomById(action.roomId()).isEmpty()) {
                throw new InvalidAutomationException("Action targets unknown room " + action.roomId());
            }
        }
    }

    public AutomationScenario getScenario(String scenarioId) {
        return manager.getHome().findScenarioById(scenarioId)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
    }

    public List<AutomationScenario> listScenarios() {
        return manager.getHome().getScenarios();
    }

    /** Partial update of a scenario. Every field left null keeps its current value. */
    public record ScenarioChanges(String name, String description, Trigger trigger, List<Action> actions,
                                  Boolean enabled, SecurityAction securityAction, Boolean notifyOnExecution,
                                  String notificationMessage) {
    }

    public AutomationScenario updateScenario(String scenarioId, ScenarioChanges changes) {
        AutomationScenario scenario = getScenario(scenarioId);
        if (changes.actions() != null) {
            validateActions(changes.actions());
        }
        if (changes.name() != null) {
            scenario.setName(changes.name());
        }
        if (changes.description() != null) {
            scenario.setDescription(changes.description());
        }
        if (changes.trigger() != null) {
            scenario.setTrigger(changes.trigger());
        }
        if (changes.actions() != null) {
            scenario.replaceActions(changes.actions());
        }
        if (changes.enabled() != null) {
            if (changes.enabled()) {
                scenario.enable();
            } else {
                scenario.disable();
            }
        }
        if (changes.securityAction() != null) {
            scenario.setSecurityAction(changes.securityAction());
        }
        if (changes.notifyOnExecution() != null) {
            scenario.setNotifyOnExecution(changes.notifyOnExecution());
        }
        if (changes.notificationMessage() != null) {
            scenario.setNotificationMessage(changes.notificationMessage());
        }
        return scenario;
    }

    public AutomationScenario deleteScenario(String scenarioId) {
        return manager.getHome().removeScenario(scenarioId)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
    }

    public ExecutionReport execute(String scenarioId) {
        return run(getScenario(scenarioId), "manually");
    }

    private ExecutionReport run(AutomationScenario scenario, String cause) {
        ExecutionReport report = scenario.execute(manager.getHome());
        if (scenario.isNotifyOnExecution()) {
            String message = scenario.getNotificationMessage() != null
                    ? scenario.getNotificationMessage()
                    : "Scenario '" + scenario.getName() + "' ran " + cause + ": " + report.summary();
            notifications.notify(NotificationType.SCENARIO, message);
        }
        return report;
    }

    /** Prototype: creates an independent copy of an existing scenario under a new id and name. */
    public AutomationScenario cloneScenario(String scenarioId, String newName) {
        AutomationScenario original = getScenario(scenarioId);
        AutomationScenario clone = original.copy();
        clone.setId(IdGenerator.next("scn"));
        clone.setName(newName == null || newName.isBlank() ? original.getName() + " (copy)" : newName);
        manager.getHome().addScenario(clone);
        return clone;
    }

    public AutomationScenario enable(String scenarioId) {
        AutomationScenario scenario = getScenario(scenarioId);
        scenario.enable();
        return scenario;
    }

    public AutomationScenario disable(String scenarioId) {
        AutomationScenario scenario = getScenario(scenarioId);
        scenario.disable();
        return scenario;
    }

    // ---- templates (Prototype registry) ----

    public Map<String, AutomationScenario> listTemplates() {
        return templates.getPrototypes();
    }

    /** Adds a new scenario to the home, cloned from a template. */
    public AutomationScenario createFromTemplate(String key, String name) {
        AutomationScenario scenario = templates.create(key);
        scenario.setId(IdGenerator.next("scn"));
        if (name != null && !name.isBlank()) {
            scenario.setName(name);
        }
        return save(scenario);
    }

    /** Stores a copy of one of the home's scenarios as a reusable template. */
    public AutomationScenario saveAsTemplate(String scenarioId, String key) {
        templates.register(key, getScenario(scenarioId));
        return getScenario(scenarioId);
    }

    // ---- automatic triggers ----

    /**
     * Fires a named event: runs every enabled EVENT scenario listening to it.
     * Automatic triggers only run while auto mode is enabled in the home configuration.
     */
    public List<ExecutionReport> fireEvent(String event) {
        if (event == null || event.isBlank()) {
            throw new IllegalArgumentException("Event name is required");
        }
        return runAutomatic(TriggerType.EVENT, event.trim());
    }

    /** Runs every enabled TIME scenario scheduled at the given minute. Called by {@link AutomationScheduler}. */
    public List<ExecutionReport> runScheduled(LocalTime now) {
        return runAutomatic(TriggerType.TIME, now.format(HH_MM));
    }

    private List<ExecutionReport> runAutomatic(TriggerType type, String value) {
        SmartHome home = manager.getHome();
        if (!home.getConfiguration().isAutoModeEnabled()) {
            return List.of();
        }
        String cause = type == TriggerType.TIME ? "at " + value : "on event '" + value + "'";
        return home.getScenarios().stream()
                .filter(AutomationScenario::isEnabled)
                .filter(s -> s.getTrigger().type() == type && value.equalsIgnoreCase(s.getTrigger().value()))
                .toList().stream()
                .map(s -> run(s, cause))
                .toList();
    }
}
