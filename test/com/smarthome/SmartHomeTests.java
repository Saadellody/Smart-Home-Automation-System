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
import com.smarthome.service.NotificationService;
import com.smarthome.service.Services;
import com.smarthome.cli.CommandLineInterface;
import com.smarthome.exception.SecurityModeException;
import com.smarthome.model.Camera;
import com.smarthome.model.DoorLock;
import com.smarthome.model.enums.SecurityAction;
import com.smarthome.model.enums.TemperatureUnit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
    private final Services services = Services.create(manager);
    private final SmartHomeService homes = services.homes();
    private final RoomService rooms = services.rooms();
    private final DeviceService devices = services.devices();
    private final AutomationService automations = services.automations();
    private final NotificationService notifications = services.notifications();

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

    public void testLightTurnedOnAfterDimmingToZeroIsBright() {
        Room room = rooms.createRoom("Living", RoomType.LIVING_ROOM);
        Light light = (Light) devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        light.setBrightness(0);
        devices.turnOn(light.getId());
        check(light.isOn() && light.getBrightness() == 100, "brightness restored");
    }

    // ------------------------------------------------------------------ temperature unit

    public void testFahrenheitConversion() {
        check(TemperatureUnit.FAHRENHEIT.fromCelsius(21) == 69.8, "21C = 69.8F");
        check(Math.abs(TemperatureUnit.FAHRENHEIT.toCelsius(212) - 100) < 1e-9, "212F = 100C");
        homes.updateConfiguration(TemperatureUnit.FAHRENHEIT, 68.0, null, null, null);
        check(homes.getConfiguration().getDefaultTemperature() == 20, "68F stored as 20C");
        check(homes.getConfiguration().fromCelsius(homes.getConfiguration().getDefaultTemperature()) == 68, "shown as 68F");
    }

    // ------------------------------------------------------------------ security mode

    public void testArmingLocksDoorsRecordsCamerasAndBlocksUnlocking() {
        Room room = rooms.createRoom("Garage", RoomType.GARAGE);
        DoorLock lock = (DoorLock) devices.addDevice(room.getId(), DeviceType.DOOR_LOCK, "Door");
        Camera camera = (Camera) devices.addDevice(room.getId(), DeviceType.CAMERA, "Cam");
        lock.unlock();

        homes.updateConfiguration(null, null, true, null, null);
        check(lock.isLocked() && camera.isRecording(), "armed: locked and recording");
        expect(SecurityModeException.class, () -> devices.executeCommand(lock.getId(), ActionType.UNLOCK, null));
        expect(SecurityModeException.class, () -> devices.turnOff(camera.getId()));
        devices.executeCommand(lock.getId(), ActionType.LOCK, null); // strengthening is still allowed

        homes.disarmSecurity();
        devices.executeCommand(lock.getId(), ActionType.UNLOCK, null);
        check(!lock.isLocked(), "unlocked after disarm");
    }

    public void testScenarioSecurityModes() {
        Room room = rooms.createRoom("Hall", RoomType.LIVING_ROOM);
        DoorLock lock = (DoorLock) devices.addDevice(room.getId(), DeviceType.DOOR_LOCK, "Door");
        AutomationScenario night = automations.createScenario(AutomationScenarioBuilder.scenario("Night")
                .turnOffAll(DeviceType.LIGHT).armSecurity());
        AutomationScenario unlockWhileArmed = automations.createScenario(AutomationScenarioBuilder.scenario("Bad")
                .addAction(Action.forAll(DeviceType.DOOR_LOCK, ActionType.UNLOCK)));
        AutomationScenario morning = automations.createScenario(AutomationScenarioBuilder.scenario("Morning")
                .disarmSecurity().addAction(Action.forAll(DeviceType.DOOR_LOCK, ActionType.UNLOCK)));

        automations.execute(night.getId());
        check(manager.getHome().isSecurityArmed(), "night arms security");
        var report = automations.execute(unlockWhileArmed.getId());
        check(report.count(com.smarthome.model.ExecutionReport.Outcome.FAILED) == 1 && lock.isLocked(), "unlock blocked");
        automations.execute(morning.getId());
        check(!manager.getHome().isSecurityArmed() && !lock.isLocked(), "morning disarms first, then unlocks");
    }

    // ------------------------------------------------------------------ notifications

    public void testNotificationsRecordedOnlyWhenEnabled() {
        Room room = rooms.createRoom("Living", RoomType.LIVING_ROOM);
        devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        AutomationScenario s = automations.createScenario(AutomationScenarioBuilder.scenario("Lights off")
                .turnOffAll(DeviceType.LIGHT));
        AutomationScenario custom = automations.createScenario(AutomationScenarioBuilder.scenario("Custom")
                .turnOffAll(DeviceType.LIGHT).notifyWith("All lights are off"));
        AutomationScenario quiet = automations.createScenario(AutomationScenarioBuilder.scenario("Quiet")
                .turnOffAll(DeviceType.LIGHT).silent());

        automations.execute(s.getId());
        automations.execute(custom.getId());
        automations.execute(quiet.getId());
        check(notifications.list().size() == 2, "silent scenario sends nothing");
        check(notifications.list().get(0).message().equals("All lights are off"), "custom message, newest first");
        check(notifications.list().get(1).message().contains("1 applied"), "generated summary");

        homes.updateConfiguration(null, null, null, false, null);
        automations.execute(s.getId());
        homes.armSecurity();
        check(notifications.list().size() == 2, "disabled -> nothing recorded");
        notifications.clear();
        check(notifications.list().isEmpty(), "cleared");
    }

    // ------------------------------------------------------------------ templates (Prototype registry)

    public void testTemplatesCreateIndependentScenarios() {
        Room room = rooms.createRoom("Living", RoomType.LIVING_ROOM);
        devices.addDevice(room.getId(), DeviceType.LIGHT, "L");
        check(automations.listTemplates().containsKey("good-night"), "built-in template");

        AutomationScenario a = automations.createFromTemplate("good-night", null);
        AutomationScenario b = automations.createFromTemplate("Good Night", "Night 2");
        check(!a.getId().equals(b.getId()) && b.getName().equals("Night 2"), "independent copies");
        check(a.getSecurityAction() == SecurityAction.ARM, "template security kept");
        a.addAction(Action.forAll(DeviceType.FAN, ActionType.TURN_OFF));
        check(automations.listTemplates().get("good-night").getActions().size() == b.getActions().size(),
                "editing a copy does not change the template");

        automations.saveAsTemplate(a.getId(), "My Night");
        check(automations.createFromTemplate("my-night", null).getActions().size() == a.getActions().size(), "saved template");
        expect(com.smarthome.exception.ResourceNotFoundException.class, () -> automations.createFromTemplate("nope", null));
    }

    // ------------------------------------------------------------------ CLI

    public void testCommandLineInterface() throws Exception {
        String script = String.join("\n",
                "room add OFFICE My Office",
                "device add room-%1$s LIGHT Desk Lamp",
                "set unit F",
                "device add room-%1$s THERMOSTAT Office Thermostat",
                "scenario new",
                "Work",
                "Focus lighting",
                "event start-work",
                "none",
                "Time to work",
                "LIGHT SET_BRIGHTNESS 70",
                "THERMOSTAT SET_TEMPERATURE 70",
                "",
                "set automode on",
                "event start-work",
                "notifications",
                "bogus",
                "exit");
        // Room ids come from a global counter, so ask the service which id the next room will get.
        String nextRoomNumber = String.valueOf(Integer.parseInt(
                rooms.createRoom("probe", RoomType.OFFICE).getId().substring(5)) + 1);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandLineInterface(services, new BufferedReader(new StringReader(String.format(script, nextRoomNumber))),
                new PrintStream(buffer, true, StandardCharsets.UTF_8)).run();
        String output = buffer.toString(StandardCharsets.UTF_8);

        Light lamp = (Light) devices.listDevices(DeviceType.LIGHT, null, null).get(0);
        Thermostat thermostat = (Thermostat) devices.listDevices(DeviceType.THERMOSTAT, null, null).get(0);
        check(lamp.isOn() && lamp.getBrightness() == 70, "event ran the scenario: " + output);
        check(Math.abs(thermostat.getTargetTemperature() - 21.1) < 0.1, "70F stored as ~21.1C");
        check(output.contains("Time to work"), "notification printed");
        check(output.contains("Unknown command 'bogus'"), "unknown command reported");
        check(output.contains("Bye."), "exit");
    }

    // ------------------------------------------------------------------ REST API

    public void testRestApiEndToEnd() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ApiServer server = new ApiServer("localhost", 0, executor, homes, rooms, devices, automations, notifications);
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

            // Fahrenheit: thermostat values in and out of the API follow the configured unit.
            call("PUT", base + "/home/configuration", "{\"temperatureUnit\":\"FAHRENHEIT\"}");
            String thermoId = (String) call("POST", base + "/devices",
                    "{\"roomId\":\"" + roomId + "\",\"type\":\"THERMOSTAT\",\"name\":\"T\"}").map().get("id");
            Http heat = call("POST", base + "/devices/" + thermoId + "/commands", "{\"type\":\"SET_TEMPERATURE\",\"value\":77}");
            check(toDouble(heat.map().get("targetTemperature")) == 77.0, "77F round-trips: " + heat.body);
            check(((Thermostat) devices.getDevice(thermoId)).getTargetTemperature() == 25.0, "stored as 25C");

            // Security, notifications, templates.
            Http armed = call("POST", base + "/home/security/arm", null);
            check(armed.map().get("securityArmed") == Boolean.TRUE, "armed");
            String doorId = (String) call("GET", base + "/devices?type=DOOR_LOCK", null).list().stream()
                    .map(d -> ((Map<?, ?>) d).get("id")).findFirst().orElseThrow();
            check(call("POST", base + "/devices/" + doorId + "/commands", "{\"type\":\"UNLOCK\"}").status == 422, "unlock blocked");
            check(!call("GET", base + "/notifications", null).list().isEmpty(), "notifications recorded");
            check(call("DELETE", base + "/notifications", null).status == 204, "notifications cleared");
            check(call("POST", base + "/home/security/disarm", null).map().get("securityArmed") == Boolean.FALSE, "disarmed");

            check(call("GET", base + "/templates", null).list().size() >= 5, "templates listed");
            Http fromTemplate = call("POST", base + "/templates/good-night/create", "{\"name\":\"Bedtime\"}");
            check(fromTemplate.status == 201 && "ARM".equals(fromTemplate.map().get("securityMode")), "template used: " + fromTemplate.body);
            Http updated = call("PUT", base + "/scenarios/" + fromTemplate.map().get("id"),
                    "{\"securityMode\":\"NONE\",\"notifyOnExecution\":false}");
            check("NONE".equals(updated.map().get("securityMode")) && updated.map().get("notifyOnExecution") == Boolean.FALSE, "updated");
            check(call("POST", base + "/scenarios/" + fromTemplate.map().get("id") + "/save-as-template",
                    "{\"key\":\"bedtime\"}").status == 201, "saved as template");
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
