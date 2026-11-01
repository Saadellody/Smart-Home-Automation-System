package com.smarthome.model;

import com.smarthome.model.enums.TriggerType;

import java.util.regex.Pattern;

/**
 * What starts a scenario. Immutable, so clones of a scenario can safely share it.
 *
 * @param type  MANUAL, TIME or EVENT
 * @param value null for MANUAL, "HH:mm" for TIME, an event name for EVENT
 */
public record Trigger(TriggerType type, String value) {

    private static final Pattern TIME = Pattern.compile("([01]\\d|2[0-3]):[0-5]\\d");

    public Trigger {
        if (type == null) {
            throw new IllegalArgumentException("Trigger type is required");
        }
        switch (type) {
            case MANUAL -> value = null;
            case TIME -> {
                if (value == null || !TIME.matcher(value).matches()) {
                    throw new IllegalArgumentException("A TIME trigger needs a value in HH:mm format");
                }
            }
            case EVENT -> {
                if (value == null || value.isBlank()) {
                    throw new IllegalArgumentException("An EVENT trigger needs an event name");
                }
            }
        }
    }

    public static Trigger manual() {
        return new Trigger(TriggerType.MANUAL, null);
    }

    public static Trigger at(String time) {
        return new Trigger(TriggerType.TIME, time);
    }

    public static Trigger onEvent(String event) {
        return new Trigger(TriggerType.EVENT, event);
    }
}
