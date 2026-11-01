package com.smarthome.model.enums;

/** What a scenario does to the home's security mode when it runs. */
public enum SecurityAction {
    /** Leave security mode as it is. */
    NONE,
    /** Arm security after the scenario's actions (locks every door, starts every camera). */
    ARM,
    /** Disarm security before the scenario's actions, so they may unlock doors or stop cameras. */
    DISARM
}
