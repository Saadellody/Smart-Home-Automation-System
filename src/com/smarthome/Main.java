package com.smarthome;

import com.smarthome.api.ApiServer;
import com.smarthome.cli.ConsoleDemo;
import com.smarthome.cli.DemoData;
import com.smarthome.service.AutomationScheduler;
import com.smarthome.service.AutomationService;
import com.smarthome.service.DeviceService;
import com.smarthome.service.RoomService;
import com.smarthome.service.SmartHomeService;
import com.smarthome.singleton.SmartHomeManager;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Entry point.
 * <pre>
 * java -cp out com.smarthome.Main                 start the REST API on localhost:8080 with demo data
 * java -cp out com.smarthome.Main serve --port 9000 --host 0.0.0.0 --empty
 * java -cp out com.smarthome.Main demo            run the console walkthrough and exit
 * </pre>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        List<String> arguments = Arrays.asList(args);

        SmartHomeManager manager = SmartHomeManager.getInstance();
        SmartHomeService smartHomeService = new SmartHomeService(manager);
        RoomService roomService = new RoomService(manager);
        DeviceService deviceService = new DeviceService(manager, roomService);
        AutomationService automationService = new AutomationService(manager);

        if (arguments.contains("demo")) {
            new ConsoleDemo(smartHomeService, roomService, deviceService, automationService).run();
            return;
        }

        if (!arguments.contains("--empty")) {
            DemoData.seed(smartHomeService, roomService, deviceService, automationService);
        }

        String host = option(arguments, "--host", System.getenv().getOrDefault("HOST", "localhost"));
        int port = Integer.parseInt(option(arguments, "--port", System.getenv().getOrDefault("PORT", "8080")));

        // One thread serves HTTP requests and runs the scheduler, so the in-memory state needs no locking.
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ApiServer server = new ApiServer(host, port, executor,
                smartHomeService, roomService, deviceService, automationService);
        server.start();
        new AutomationScheduler(automationService, executor, Clock.systemDefaultZone()).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            executor.shutdownNow();
        }));
        System.out.println("Smart Home API running on http://" + host + ":" + server.getPort() + "/api");
    }

    private static String option(List<String> args, String name, String defaultValue) {
        int index = args.indexOf(name);
        return index >= 0 && index + 1 < args.size() ? args.get(index + 1) : defaultValue;
    }
}
