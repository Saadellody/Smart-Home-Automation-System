package com.smartphone.model;

import com.smartphone.model.authomationScenarioneeds.Action;
import com.smartphone.model.authomationScenarioneeds.trigger;

import java.util.List;

public class AutomationScenario {

    private String id;
    private String name;
    private String description;
    private trigger trigger;
    private List<Action> actions;
    private boolean enabled;

    public AutomationScenario(String id, String name, String description, trigger trigger, List<Action> actions, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.trigger = trigger;
        this.actions = actions;
        this.enabled = enabled;
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
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(trigger trigger) {
        this.trigger = trigger;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
//    Methods of AutomationScenario
//
//    Yes, it should have methods.
//
//    Add an action
//    addAction(Action action)
//
//    Allows you to add actions to the scenario.
//
//    Remove an action
//    removeAction(Action action)
//    Enable / disable
//    enable()
//    disable()
//    Check if enabled
//    isEnabled()
//    Execute
//    execute()
//
//    This is important, but don't implement the complex execution logic yet.
}
