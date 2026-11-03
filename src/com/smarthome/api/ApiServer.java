package com.smarthome.api;

import com.smarthome.api.controller.DeviceController;
import com.smarthome.api.controller.HomeController;
import com.smarthome.api.controller.RoomController;
import com.smarthome.api.controller.ScenarioController;
import com.smarthome.service.AutomationService;
import com.smarthome.service.DeviceService;
import com.smarthome.service.NotificationService;
import com.smarthome.service.RoomService;
import com.smarthome.service.SmartHomeService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executor;

/** REST API (under /api) and web dashboard (at /), built on the JDK's HttpServer: no framework, no external dependency. */
public class ApiServer {

    private final HttpServer server;
    private final Router router = new Router();
    private final StaticFileHandler dashboard;

    public ApiServer(String host, int port, Executor executor,
                     SmartHomeService smartHomeService, RoomService roomService,
                     DeviceService deviceService, AutomationService automationService,
                     NotificationService notificationService) throws IOException {
        new HomeController(smartHomeService, notificationService).register(router);
        new RoomController(roomService, deviceService).register(router);
        new DeviceController(deviceService).register(router);
        new ScenarioController(automationService).register(router);
        router.get("/api", req -> Response.ok(Map.of("name", "Smart Home Automation API", "endpoints", router.describeRoutes())));

        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/api", router);
        dashboard = new StaticFileHandler(StaticFileHandler.locateWebRoot());
        server.createContext("/", dashboard);
        server.setExecutor(executor);
    }

    /** True when the web dashboard files were found and are served at "/". */
    public boolean isDashboardAvailable() {
        return dashboard.isAvailable();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }
}
