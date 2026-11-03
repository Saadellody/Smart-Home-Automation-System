package com.smarthome;

import com.smarthome.api.ApiServer;
import com.smarthome.cli.CommandLineInterface;
import com.smarthome.cli.ConsoleDemo;
import com.smarthome.cli.DemoData;
import com.smarthome.service.AutomationScheduler;
import com.smarthome.service.Services;
import com.smarthome.singleton.SmartHomeManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
 * java -cp out com.smarthome.Main cli [--empty]   interactive command line
 * java -cp out com.smarthome.Main demo            run the console walkthrough and exit
 * </pre>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        List<String> arguments = Arrays.asList(args);
        Services services = Services.create(SmartHomeManager.getInstance());

        if (arguments.contains("demo")) {
            new ConsoleDemo(services).run();
            return;
        }

        if (!arguments.contains("--empty")) {
            DemoData.seed(services);
        }

        if (arguments.contains("cli")) {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            new CommandLineInterface(services, in, System.out).run();
            return;
        }

        String host = option(arguments, "--host", System.getenv().getOrDefault("HOST", "localhost"));
        int port = Integer.parseInt(option(arguments, "--port", System.getenv().getOrDefault("PORT", "8080")));

        // One thread serves HTTP requests and runs the scheduler, so the in-memory state needs no locking.
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ApiServer server = new ApiServer(host, port, executor, services.homes(), services.rooms(),
                services.devices(), services.automations(), services.notifications());
        server.start();
        new AutomationScheduler(services.automations(), executor, Clock.systemDefaultZone()).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            executor.shutdownNow();
        }));
        String base = "http://" + host + ":" + server.getPort();
        System.out.println("Smart Home API running on " + base + "/api");
        System.out.println(server.isDashboardAvailable()
                ? "Dashboard: " + base + "/"
                : "Dashboard files (web/index.html) not found; run from the project folder to get the dashboard.");
    }

    private static String option(List<String> args, String name, String defaultValue) {
        int index = args.indexOf(name);
        return index >= 0 && index + 1 < args.size() ? args.get(index + 1) : defaultValue;
    }
}
