package com.smarthome.service;

import com.smarthome.model.ExecutionReport;

import java.time.Clock;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Checks TIME-triggered scenarios a few times per minute and runs each scheduled minute once.
 * It runs on the same single-thread executor as the HTTP server, so it never races with API requests.
 */
public class AutomationScheduler {

    private final AutomationService automationService;
    private final ScheduledExecutorService executor;
    private final Clock clock;
    private LocalTime lastRunMinute;

    public AutomationScheduler(AutomationService automationService, ScheduledExecutorService executor, Clock clock) {
        this.automationService = automationService;
        this.executor = executor;
        this.clock = clock;
    }

    public void start() {
        executor.scheduleAtFixedRate(this::tick, 0, 15, TimeUnit.SECONDS);
    }

    void tick() {
        LocalTime minute = LocalTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        if (minute.equals(lastRunMinute)) {
            return;
        }
        lastRunMinute = minute;
        try {
            List<ExecutionReport> reports = automationService.runScheduled(minute);
            reports.forEach(r -> System.out.println("[scheduler] " + minute + " ran '" + r.getScenarioName() + "'"));
        } catch (RuntimeException e) {
            System.err.println("[scheduler] " + e.getMessage());
        }
    }
}
