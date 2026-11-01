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
import com.smarthome.model.enums.TriggerType;
import com.smarthome.singleton.SmartHomeManager;
import com.smarthome.util.IdGenerator;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AutomationService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final SmartHomeManager manager;

    public AutomationService(SmartHomeManager manager) {
        this.manager = manager;
    }

    public AutomationScenario createScenario(AutomationScenarioBuilder builder) {
        AutomationScenario scenario = builder.build();
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

    /** Updates a scenario; null arguments leave the current value unchanged. */
    public AutomationScenario updateScenario(String scenarioId, String name, String description,
                                             Trigger trigger, List<Action> actions, Boolean enabled) {
        AutomationScenario scenario = getScenario(scenarioId);
        if (actions != null) {
            validateActions(actions);
        }
        if (name != null) {
            scenario.setName(name);
        }
        if (description != null) {
            scenario.setDescription(description);
        }
        if (trigger != null) {
            scenario.setTrigger(trigger);
        }
        if (actions != null) {
            scenario.replaceActions(actions);
        }
        if (enabled != null) {
            if (enabled) {
                scenario.enable();
            } else {
                scenario.disable();
            }
        }
        return scenario;
    }

    public AutomationScenario deleteScenario(String scenarioId) {
        return manager.getHome().removeScenario(scenarioId)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
    }

    public ExecutionReport execute(String scenarioId) {
        return getScenario(scenarioId).execute(manager.getHome());
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
        return home.getScenarios().stream()
                .filter(AutomationScenario::isEnabled)
                .filter(s -> s.getTrigger().type() == type && value.equalsIgnoreCase(s.getTrigger().value()))
                .map(s -> s.execute(home))
                .toList();
    }
}
