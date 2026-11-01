package com.smarthome.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Result of running a scenario: what happened for each action/device pair. */
public class ExecutionReport {

    public enum Outcome { APPLIED, SKIPPED, FAILED }

    public record Entry(String action, String deviceId, String deviceName, Outcome outcome, String message) {
    }

    private final String scenarioId;
    private final String scenarioName;
    private final List<Entry> entries = new ArrayList<>();

    public ExecutionReport(String scenarioId, String scenarioName) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
    }

    void applied(Action action, Device device) {
        entries.add(new Entry(action.describe(), device.getId(), device.getName(), Outcome.APPLIED, null));
    }

    void skipped(Action action, String reason) {
        entries.add(new Entry(action.describe(), null, null, Outcome.SKIPPED, reason));
    }

    void failed(Action action, Device device, String reason) {
        entries.add(new Entry(action.describe(), device.getId(), device.getName(), Outcome.FAILED, reason));
    }

    void security(String state) {
        entries.add(new Entry("security -> " + state, null, null, Outcome.APPLIED, null));
    }

    /** "3 applied, 1 skipped, 0 failed" */
    public String summary() {
        return count(Outcome.APPLIED) + " applied, " + count(Outcome.SKIPPED) + " skipped, "
                + count(Outcome.FAILED) + " failed";
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public long count(Outcome outcome) {
        return entries.stream().filter(e -> e.outcome() == outcome).count();
    }
}
