package com.smarthome.model.enums;

/**
 * What starts an automation scenario.
 * MANUAL is executed on demand, TIME carries a "HH:mm" value, EVENT carries a free-form event name.
 */
public enum TriggerType {
    MANUAL,
    TIME,
    EVENT
}
