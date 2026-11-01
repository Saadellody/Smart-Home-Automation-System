package com.smarthome.builder;

import com.smarthome.exception.InvalidAutomationException;
import com.smarthome.model.Action;
import com.smarthome.model.AutomationScenario;
import com.smarthome.model.Trigger;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.SecurityAction;
import com.smarthome.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder: a scenario has a required name and many optional parts: description, trigger (schedule),
 * actions, temperature, lighting, security and notification settings. Building it step by step keeps
 * call sites readable:
 *
 * <pre>
 * AutomationScenario goodNight = AutomationScenarioBuilder.scenario("Good Night")
 *         .at("22:30")
 *         .lighting(0)
 *         .turnOffAll(DeviceType.TV)
 *         .temperature(19)
 *         .armSecurity()
 *         .notifyWith("Good night! The house is locked.")
 *         .build();
 * </pre>
 */
public class AutomationScenarioBuilder {

    private String id;
    private final String name;
    private String description;
    private Trigger trigger = Trigger.manual();
    private final List<Action> actions = new ArrayList<>();
    private boolean enabled = true;
    private SecurityAction securityAction = SecurityAction.NONE;
    private boolean notifyOnExecution = true;
    private String notificationMessage;

    public AutomationScenarioBuilder(String name) {
        this.name = name;
    }

    public static AutomationScenarioBuilder scenario(String name) {
        return new AutomationScenarioBuilder(name);
    }

    public AutomationScenarioBuilder id(String id) {
        this.id = id;
        return this;
    }

    public AutomationScenarioBuilder description(String description) {
        this.description = description;
        return this;
    }

    public AutomationScenarioBuilder trigger(Trigger trigger) {
        this.trigger = trigger;
        return this;
    }

    public AutomationScenarioBuilder manual() {
        return trigger(Trigger.manual());
    }

    /** Runs every day at the given "HH:mm" time (when auto mode is enabled). */
    public AutomationScenarioBuilder at(String time) {
        return trigger(Trigger.at(time));
    }

    /** Runs when the named event is fired (when auto mode is enabled). */
    public AutomationScenarioBuilder onEvent(String event) {
        return trigger(Trigger.onEvent(event));
    }

    public AutomationScenarioBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public AutomationScenarioBuilder addAction(Action action) {
        if (action == null) {
            throw new InvalidAutomationException("Action is required");
        }
        actions.add(action);
        return this;
    }

    public AutomationScenarioBuilder addActions(List<Action> actions) {
        actions.forEach(this::addAction);
        return this;
    }

    // ---- shortcuts for common steps ----

    public AutomationScenarioBuilder turnOn(String deviceId) {
        return addAction(Action.forDevice(deviceId, ActionType.TURN_ON));
    }

    public AutomationScenarioBuilder turnOff(String deviceId) {
        return addAction(Action.forDevice(deviceId, ActionType.TURN_OFF));
    }

    public AutomationScenarioBuilder turnOnAll(DeviceType type) {
        return addAction(Action.forAll(type, ActionType.TURN_ON));
    }

    public AutomationScenarioBuilder turnOffAll(DeviceType type) {
        return addAction(Action.forAll(type, ActionType.TURN_OFF));
    }

    public AutomationScenarioBuilder brightness(String lightId, int percent) {
        return addAction(Action.forDevice(lightId, ActionType.SET_BRIGHTNESS, (double) percent));
    }

    /** Sets every thermostat of the home to the given temperature (Celsius). */
    public AutomationScenarioBuilder temperature(double celsius) {
        return addAction(Action.forAll(DeviceType.THERMOSTAT, ActionType.SET_TEMPERATURE, celsius));
    }

    public AutomationScenarioBuilder lockAllDoors() {
        return addAction(Action.forAll(DeviceType.DOOR_LOCK, ActionType.LOCK));
    }

    public AutomationScenarioBuilder startRecordingAllCameras() {
        return addAction(Action.forAll(DeviceType.CAMERA, ActionType.START_RECORDING));
    }

    // ---- lighting configuration ----

    /** Sets every light of the home to the given brightness (0 turns them off). */
    public AutomationScenarioBuilder lighting(int percent) {
        return addAction(Action.forAll(DeviceType.LIGHT, ActionType.SET_BRIGHTNESS, (double) percent));
    }

    /** Sets every light of one room to the given brightness. */
    public AutomationScenarioBuilder lighting(String roomId, int percent) {
        return addAction(Action.forAllInRoom(roomId, DeviceType.LIGHT, ActionType.SET_BRIGHTNESS, (double) percent));
    }

    // ---- security configuration ----

    /** Arms security mode once the actions have run (doors locked, cameras recording). */
    public AutomationScenarioBuilder armSecurity() {
        this.securityAction = SecurityAction.ARM;
        return this;
    }

    /** Disarms security mode before the actions run, so they may unlock doors. */
    public AutomationScenarioBuilder disarmSecurity() {
        this.securityAction = SecurityAction.DISARM;
        return this;
    }

    public AutomationScenarioBuilder security(SecurityAction securityAction) {
        this.securityAction = securityAction;
        return this;
    }

    // ---- notification configuration ----

    /** Sends this text instead of the generated summary when the scenario runs. */
    public AutomationScenarioBuilder notifyWith(String message) {
        this.notifyOnExecution = true;
        this.notificationMessage = message;
        return this;
    }

    public AutomationScenarioBuilder notifyOnExecution(boolean notify) {
        this.notifyOnExecution = notify;
        return this;
    }

    public AutomationScenarioBuilder silent() {
        return notifyOnExecution(false);
    }

    public AutomationScenario build() {
        if (name == null || name.isBlank()) {
            throw new InvalidAutomationException("Scenario name is required");
        }
        if (actions.isEmpty()) {
            throw new InvalidAutomationException("Scenario '" + name + "' needs at least one action");
        }
        String scenarioId = id == null ? IdGenerator.next("scn") : id;
        return new AutomationScenario(scenarioId, name, description, trigger, actions, enabled,
                securityAction, notifyOnExecution, notificationMessage);
    }
}
