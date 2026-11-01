package com.smarthome;

import com.smarthome.abstractfactory.SmartHomeFactory;
import com.smarthome.abstractfactory.apple.AppleLight;
import com.smarthome.api.ApiServer;
import com.smarthome.api.Json;
import com.smarthome.builder.AutomationScenarioBuilder;
import com.smarthome.exception.InvalidAutomationException;
import com.smarthome.exception.InvalidDeviceOperationException;
import com.smarthome.model.Action;
import com.smarthome.model.AutomationScenario;
import com.smarthome.model.Device;
import com.smarthome.model.Light;
import com.smarthome.model.Room;
import com.smarthome.model.Thermostat;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;
import com.smarthome.model.enums.RoomType;
import com.smarthome.service.AutomationService;
import com.smarthome.service.DeviceService;
import com.smarthome.service.RoomService;
import com.smarthome.service.SmartHomeService;
import com.smarthome.singleton.SmartHomeManager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dependency-free test suite (no JUnit needed): every public void method starting with "test" is run.
 * <pre>
 * javac -d out $(find src test -name "*.java") && java -cp out com.smarthome.SmartHomeTests
 * </pre>
 */
public class SmartHomeTests {

    private final SmartHomeManager manager = SmartHomeManager.getInstance();
    private final SmartHomeService homes = new SmartHomeService(manager);
    private final RoomService rooms = new RoomService(manager);
    private final DeviceService devices = new DeviceService(manager, rooms);
    private final AutomationService automations = new AutomationService(manager);

    public static void main(String[] args) throws Exception {
        SmartHomeTests suite = new SmartHomeTests();
        int passed = 0;
        int failed = 0;
        for (Method method : SmartHomeTests.class.getDeclaredMethods()) {
            if (!method.getName().startsWith("test") || !Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            suite.homes.createHome("Test Home", Ecosystem.GENERIC);
            try {
                method.invoke(suite);
                passed++;
                System.out.println("PASS " + method.getName());
            } catch (java.lang.reflect.InvocationTargetException e) {
                failed++;
                System.out.println("FAIL " + method.getName() + ": " + e.getCause());
            }
        }
        System.out.println(passed + " passed, " + failed + " failed");
        System.exit(failed > 0 ? 1 : 0);
    }

    // ------------------------------------------------------------------ patterns

    public void testSingletonReturnsSameInstance() {
        check(SmartHomeManager.getInstance() == SmartHomeManager.getInstance(), "same instance");
    }

    public void testFactoryMethodAndAbstractFactoryCreateEcosystemDevices() {
        homes.createHome("Apple Home", Ecosystem.APPLE);
        Room room = rooms.createRoom("Office", RoomType.OFFICE);
        Device light = devices.addDevice(room.getId(), DeviceType.LIGHT, "Desk Light");
        Device tv = devices.addDevice(room.getId(), DeviceType.TV, "TV");
        check(light instanceof AppleLight, "light comes from the Apple family");
        check(light.getEcosystem() == Ecosystem.APPLE, "light ecosystem");
        check(tv.getType() == DeviceType.TV && tv.getEcosystem() == Ecosystem.GENERIC, "tv is generic");
        check(SmartHomeFactory.forEcosystem(Ecosystem.GOOGLE).createCamera("c", "Cam").getEcosystem() == Ecosystem.GOOGLE,
                "google camera");
    }

    public void testNewThermostatUsesDefaultTemperature() {
        homes.updateConfiguration(null, 18.5, null, null, null);
        Room room = rooms.createRoom("Hall", RoomType.LIVING_ROOM);
        Thermostat t = (Thermostat) devices.addDevice(room.getId(), DeviceType.THERMOSTAT, "T");
        check(t.getTargetTemperature() == 18.5, "default temperature applied");
        check(!t.isOn(), "new thermostat is off");
    }

    public void testBuilderRejectsScenarioWithoutActions() {
        expect(InvalidAutomationException.class, () -> AutomationScenarioBuilder.scenario("Empty").build());
        expect(InvalidAutomationException.class, () -> AutomationScenarioBuilder.scenario(" ").turnOffAll(DeviceType.LIGHT).build());
    }

    public void testServiceRejectsActionOnUnknownOrIncompatibleDevice() {
        Room room = rooms.createRoom("Kitchen", RoomType.KITCHEN);
        Device light = devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        expect(InvalidAutomationException.class, () -> automations.createScenario(
                AutomationScenarioBuilder.scenario("X").turnOn("dev-does-not-exist")));
        expect(InvalidAutomationException.class, () -> automations.createScenario(
                AutomationScenarioBuilder.scenario("X").addAction(Action.forDevice(light.getId(), ActionType.LOCK))));
    }

    public void testPrototypeCloneIsIndependent() {
        Room room = rooms.createRoom("Living", RoomType.LIVING_ROOM);
        devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        AutomationScenario original = automations.createScenario(
                AutomationScenarioBuilder.scenario("Movie").turnOffAll(DeviceType.LIGHT));
        AutomationScenario clone = automations.cloneScenario(original.getId(), "Movie Winter");
        clone.addAction(Action.forAll(DeviceType.THERMOSTAT, ActionType.SET_TEMPERATURE, 23.0));
        clone.disable();
        check(!clone.getId().equals(original.getId()), "new id");
        check(original.getActions().size() == 1 && clone.getActions().size() == 2, "actions are independent");
        check(original.isEnabled() && !clone.isEnabled(), "flags are independent");
        check(automations.listScenarios().size() == 2, "clone stored");
    }

    public void testExecuteAppliesActionsAndReportsMissingDevices() {
        Room room = rooms.createRoom("Living", RoomType.LIVING_ROOM);
        Light light = (Light) devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        AutomationScenario s = automations.createScenario(AutomationScenarioBuilder.scenario("Dim")
                .brightness(light.getId(), 30)
                .lockAllDoors());
        var report = automations.execute(s.getId());
        check(light.isOn() && light.getBrightness() == 30, "light dimmed");
        check(report.getEntries().size() == 2, "two entries");
        check(report.getEntries().get(1).outcome().name().equals("SKIPPED"), "no door lock -> skipped");
        check(s.getId().equals(manager.getHome().getActiveScenarioId()), "active scenario tracked");
    }

    public void testDisabledScenarioCannotRun() {
        Room room = rooms.createRoom("Living", RoomType.LIVING_ROOM);
        devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        AutomationScenario s = automations.createScenario(
                AutomationScenarioBuilder.scenario("Off").turnOffAll(DeviceType.LIGHT).enabled(false));
        expect(InvalidAutomationException.class, () -> automations.execute(s.getId()));
    }

    public void testTimeAndEventTriggersRunOnlyInAutoMode() {
        Room room = rooms.createRoom("Bedroom", RoomType.BEDROOM);
        Light light = (Light) devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        automations.createScenario(AutomationScenarioBuilder.scenario("Wake").at("07:00").turnOn(light.getId()));
        automations.createScenario(AutomationScenarioBuilder.scenario("Arrive").onEvent("arrived-home").turnOn(light.getId()));

        check(automations.runScheduled(LocalTime.of(7, 0)).isEmpty(), "auto mode off -> nothing runs");
        homes.updateConfiguration(null, null, null, null, true);
        check(automations.runScheduled(LocalTime.of(6, 59)).isEmpty(), "wrong minute");
        check(automations.runScheduled(LocalTime.of(7, 0)).size() == 1 && light.isOn(), "scheduled run");
        light.turnOff();
        check(automations.fireEvent("ARRIVED-HOME").size() == 1 && light.isOn(), "event run");
    }

    public void testDeviceValidation() {
        Room room = rooms.createRoom("Living", RoomType.LIVING_ROOM);
        Device light = devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        expect(InvalidDeviceOperationException.class,
                () -> devices.executeCommand(light.getId(), ActionType.SET_BRIGHTNESS, 150.0));
        expect(InvalidDeviceOperationException.class,
                () -> devices.executeCommand(light.getId(), ActionType.SET_TEMPERATURE, 20.0));
    }

    public void testMovingDeviceUpdatesBothRooms() {
        Room a = rooms.createRoom("A", RoomType.OFFICE);
        Room b = rooms.createRoom("B", RoomType.OFFICE);
        Device d = devices.addDevice(a.getId(), DeviceType.FAN, "Fan");
        devices.moveDevice(d.getId(), b.getId());
        check(a.getDevices().isEmpty() && b.getDevices().contains(d) && d.getRoom() == b, "moved");
    }

    // ------------------------------------------------------------------ REST API

    public void testRestApiEndToEnd() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ApiServer server = new ApiServer("localhost", 0, executor, homes, rooms, devices, automations);
        server.start();
        String base = "http://localhost:" + server.getPort() + "/api";
        try {
            check(call("GET", base + "/health", null).status == 200, "health");

            Http home = call("POST", base + "/home", "{\"name\":\"Api Home\",\"ecosystem\":\"amazon\"}");
            check(home.status == 201 && "AMAZON".equals(home.map().get("ecosystem")), "home created");

            Http room = call("POST", base + "/rooms", "{\"name\":\"Lounge\",\"type\":\"LIVING_ROOM\"}");
            check(room.status == 201, "room created");
            String roomId = (String) room.map().get("id");

            Http light = call("POST", base + "/devices", "{\"roomId\":\"" + roomId + "\",\"type\":\"LIGHT\",\"name\":\"Lamp\"}");
            check(light.status == 201 && "Amazon Home Light".equals(light.map().get("model")), "device created");
            String lightId = (String) light.map().get("id");
            call("POST", base + "/rooms/" + roomId + "/devices", "{\"type\":\"door-lock\",\"name\":\"Door\"}");

            Http dim = call("POST", base + "/devices/" + lightId + "/commands", "{\"type\":\"SET_BRIGHTNESS\",\"value\":40}");
            check(dim.status == 200 && Double.valueOf(40).equals(toDouble(dim.map().get("brightness"))), "command");

            Http filtered = call("GET", base + "/devices?type=LIGHT&status=ON", null);
            check(filtered.list().size() == 1, "filter by type/status");

            Http scenario = call("POST", base + "/scenarios", """
                    {"name":"Night","trigger":{"type":"TIME","value":"23:00"},
                     "actions":[{"type":"TURN_OFF","deviceType":"LIGHT"},{"type":"UNLOCK","deviceType":"DOOR_LOCK"}]}""");
            check(scenario.status == 201, "scenario created: " + scenario.body);
            String scenarioId = (String) scenario.map().get("id");

            Http run = call("POST", base + "/scenarios/" + scenarioId + "/execute", null);
            check(run.status == 200 && toDouble(run.map().get("applied")) == 2, "executed: " + run.body);
            check("OFF".equals(call("GET", base + "/devices/" + lightId, null).map().get("status")), "light off");

            Http clone = call("POST", base + "/scenarios/" + scenarioId + "/clone", "{\"name\":\"Night copy\"}");
            check(clone.status == 201 && "Night copy".equals(clone.map().get("name")), "cloned");
            check(call("POST", base + "/scenarios/" + scenarioId + "/disable", null).map().get("enabled") == Boolean.FALSE, "disabled");
            check(call("POST", base + "/scenarios/" + scenarioId + "/execute", null).status == 422, "disabled -> 422");

            check(call("GET", base + "/devices/nope", null).status == 404, "404");
            check(call("POST", base + "/rooms", "{\"name\":\"X\"}").status == 400, "missing type -> 400");
            check(call("POST", base + "/rooms", "{bad json").status == 400, "bad json -> 400");
            check(call("PATCH", base + "/rooms", null).status == 405, "405");
            check(call("DELETE", base + "/devices/" + lightId, null).status == 204, "delete");
            check(call("GET", base + "/devices/" + lightId, null).status == 404, "deleted");
        } finally {
            server.stop();
            executor.shutdownNow();
        }
    }

    // ------------------------------------------------------------------ helpers

    private record Http(int status, String body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map() {
            return (Map<String, Object>) Json.parse(body);
        }

        List<?> list() {
            return (List<?>) Json.parse(body);
        }
    }

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static Http call(String method, String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return new Http(response.statusCode(), response.body());
    }

    private static Double toDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expect(Class<? extends Throwable> type, Runnable code) {
        try {
            code.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return;
            }
            throw new AssertionError("expected " + type.getSimpleName() + " but got " + t);
        }
        throw new AssertionError("expected " + type.getSimpleName());
    }
}
