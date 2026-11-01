package com.smarthome.model;

import com.smarthome.exception.InvalidAutomationException;
import com.smarthome.exception.SmartHomeException;
import com.smarthome.model.enums.SecurityAction;
import com.smarthome.prototype.Prototype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named set of actions started by a trigger, with optional security and notification settings.
 * Built with {@link com.smarthome.builder.AutomationScenarioBuilder} and duplicated with {@link #copy()}.
 */
public class AutomationScenario implements Prototype<AutomationScenario> {

    private String id;
    private String name;
    private String description;
    private Trigger trigger;
    private final List<Action> actions;
    private boolean enabled;
    private SecurityAction securityAction;
    private boolean notifyOnExecution;
    private String notificationMessage;

    public AutomationScenario(String id, String name, String description, Trigger trigger,
                              List<Action> actions, boolean enabled) {
        this(id, name, description, trigger, actions, enabled, SecurityAction.NONE, true, null);
    }

    public AutomationScenario(String id, String name, String description, Trigger trigger,
                              List<Action> actions, boolean enabled, SecurityAction securityAction,
                              boolean notifyOnExecution, String notificationMessage) {
        this.id = id;
        setName(name);
        this.description = description;
        setTrigger(trigger);
        this.actions = new ArrayList<>(actions);
        this.enabled = enabled;
        setSecurityAction(securityAction);
        this.notifyOnExecution = notifyOnExecution;
        setNotificationMessage(notificationMessage);
    }

    /**
     * Deep copy: the clone owns its own action list, so editing it never changes this scenario.
     * Actions and triggers are immutable records, so sharing the instances themselves is safe.
     */
    @Override
    public AutomationScenario copy() {
        return new AutomationScenario(id, name, description, trigger, actions, enabled,
                securityAction, notifyOnExecution, notificationMessage);
    }

    /**
     * Applies every action to its target devices.
     * One failing action (missing device, invalid value, blocked by security) is reported and does not
     * stop the others. DISARM happens before the actions, ARM after them.
     */
    public ExecutionReport execute(SmartHome home) {
        if (!enabled) {
            throw new InvalidAutomationException("Scenario " + id + " is disabled");
        }
        ExecutionReport report = new ExecutionReport(id, name);
        if (securityAction == SecurityAction.DISARM) {
            home.disarmSecurity();
            report.security("DISARMED");
        }
        for (Action action : actions) {
            List<Device> targets = action.resolveTargets(home);
            if (targets.isEmpty()) {
                report.skipped(action, "no matching device");
                continue;
            }
            for (Device device : targets) {
                try {
                    action.applyTo(device);
                    report.applied(action, device);
                } catch (SmartHomeException | IllegalArgumentException e) {
                    report.failed(action, device, e.getMessage());
                }
            }
        }
        if (securityAction == SecurityAction.ARM) {
            home.armSecurity();
            report.security("ARMED");
        }
        home.setActiveScenarioId(id);
        return report;
    }

    public void addAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action is required");
        }
        actions.add(action);
    }

    public void removeAction(int index) {
        if (index < 0 || index >= actions.size()) {
            throw new IllegalArgumentException("No action at index " + index);
        }
        if (actions.size() == 1) {
            throw new InvalidAutomationException("A scenario needs at least one action");
        }
        actions.remove(index);
    }

    public void replaceActions(List<Action> newActions) {
        if (newActions == null || newActions.isEmpty()) {
            throw new InvalidAutomationException("A scenario needs at least one action");
        }
        actions.clear();
        actions.addAll(newActions);
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidAutomationException("Scenario name is required");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger == null ? Trigger.manual() : trigger;
    }

    public SecurityAction getSecurityAction() {
        return securityAction;
    }

    public void setSecurityAction(SecurityAction securityAction) {
        this.securityAction = securityAction == null ? SecurityAction.NONE : securityAction;
    }

    public boolean isNotifyOnExecution() {
        return notifyOnExecution;
    }

    public void setNotifyOnExecution(boolean notifyOnExecution) {
        this.notifyOnExecution = notifyOnExecution;
    }

    /** Custom text sent when the scenario runs; null means a generated summary. */
    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage == null || notificationMessage.isBlank()
                ? null : notificationMessage.trim();
    }
}
