package com.smarthome.cli;

import com.smarthome.model.AutomationScenario;
import com.smarthome.model.ExecutionReport;
import com.smarthome.model.enums.ActionType;
import com.smarthome.service.AutomationService;
import com.smarthome.service.Services;
import com.smarthome.singleton.SmartHomeManager;

/** Walks through the complete workflow of SYSTEM_ARCHITECTURE.md section 13 in the console. */
public class ConsoleDemo {

    private final Services services;
    private int stepNumber;

    public ConsoleDemo(Services services) {
        this.services = services;
    }

    public void run() {
        AutomationService automations = services.automations();
        step("Singleton: one SmartHomeManager",
                "same instance twice -> " + (SmartHomeManager.getInstance() == SmartHomeManager.getInstance()));

        DemoData.seed(services);
        step("Home, rooms and devices created (Google ecosystem via Abstract Factory)", services.homes().describeState());

        StringBuilder list = new StringBuilder();
        for (AutomationScenario s : automations.listScenarios()) {
            list.append(s.getId()).append("  ").append(s.getName())
                    .append("  trigger=").append(s.getTrigger().type())
                    .append(s.getTrigger().value() == null ? "" : " " + s.getTrigger().value())
                    .append("  security=").append(s.getSecurityAction())
                    .append("  actions=").append(s.getActions().size()).append('\n');
        }
        step("Scenarios built with the Builder (Movie Mode Winter cloned with Prototype)", list.toString().trim());

        for (String name : new String[]{"Movie Mode", "Good Night"}) {
            AutomationScenario scenario = automations.listScenarios().stream()
                    .filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
            step("Execute '" + name + "'", CommandLineInterface.formatReport(automations.execute(scenario.getId())));
        }

        try {
            services.devices().executeCommand(
                    services.homes().getHome().getDevices().stream()
                            .filter(d -> d.getName().equals("Front Door Lock")).findFirst().orElseThrow().getId(),
                    ActionType.UNLOCK, null);
        } catch (RuntimeException e) {
            step("Security mode refuses to unlock the front door", e.getMessage());
        }

        ExecutionReport morning = automations.execute(automations.listScenarios().stream()
                .filter(s -> s.getName().equals("Good Morning")).findFirst().orElseThrow().getId());
        step("Execute 'Good Morning'", CommandLineInterface.formatReport(morning));

        step("Final state", services.homes().describeState());

        StringBuilder notes = new StringBuilder();
        services.notifications().list().forEach(n -> notes.append(n.type()).append("  ").append(n.message()).append('\n'));
        step("Notifications (newest first)", notes.toString().trim());
    }

    private void step(String title, String body) {
        System.out.println();
        System.out.println("== " + (++stepNumber) + ". " + title);
        System.out.println(body);
    }
}
