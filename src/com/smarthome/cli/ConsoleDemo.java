package com.smarthome.cli;

import com.smarthome.model.AutomationScenario;
import com.smarthome.model.ExecutionReport;
import com.smarthome.service.AutomationService;
import com.smarthome.service.DeviceService;
import com.smarthome.service.RoomService;
import com.smarthome.service.SmartHomeService;
import com.smarthome.singleton.SmartHomeManager;

/** Walks through the complete workflow of SYSTEM_ARCHITECTURE.md section 13 in the console. */
public class ConsoleDemo {

    private final SmartHomeService homes;
    private final RoomService rooms;
    private final DeviceService devices;
    private final AutomationService automations;

    public ConsoleDemo(SmartHomeService homes, RoomService rooms, DeviceService devices, AutomationService automations) {
        this.homes = homes;
        this.rooms = rooms;
        this.devices = devices;
        this.automations = automations;
    }

    public void run() {
        step("Singleton: one SmartHomeManager",
                "same instance twice -> " + (SmartHomeManager.getInstance() == SmartHomeManager.getInstance()));

        DemoData.seed(homes, rooms, devices, automations);
        step("Home, rooms and devices created (Google ecosystem via Abstract Factory)", homes.describeState());

        StringBuilder list = new StringBuilder();
        for (AutomationScenario s : automations.listScenarios()) {
            list.append(s.getId()).append("  ").append(s.getName())
                    .append("  trigger=").append(s.getTrigger().type())
                    .append(s.getTrigger().value() == null ? "" : " " + s.getTrigger().value())
                    .append("  actions=").append(s.getActions().size()).append('\n');
        }
        step("Scenarios built with the Builder (Movie Mode Winter cloned with Prototype)", list.toString().trim());

        for (String name : new String[]{"Movie Mode", "Good Night"}) {
            AutomationScenario scenario = automations.listScenarios().stream()
                    .filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
            ExecutionReport report = automations.execute(scenario.getId());
            StringBuilder out = new StringBuilder();
            report.getEntries().forEach(e -> out.append(e.outcome()).append("  ").append(e.action())
                    .append(e.deviceName() == null ? "" : "  (" + e.deviceName() + ")")
                    .append(e.message() == null ? "" : "  " + e.message()).append('\n'));
            step("Execute '" + name + "'", out.toString().trim());
        }

        step("Final state", homes.describeState());
    }

    private int stepNumber;

    private void step(String title, String body) {
        System.out.println();
        System.out.println("== " + (++stepNumber) + ". " + title);
        System.out.println(body);
    }
}
