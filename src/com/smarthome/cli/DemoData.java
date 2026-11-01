package com.smarthome.cli;

import com.smarthome.builder.AutomationScenarioBuilder;
import com.smarthome.model.Action;
import com.smarthome.model.AutomationScenario;
import com.smarthome.model.Device;
import com.smarthome.model.Room;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;
import com.smarthome.model.enums.RoomType;
import com.smarthome.service.DeviceService;
import com.smarthome.service.RoomService;
import com.smarthome.service.Services;

/** Fills the home with the rooms, devices and scenarios described in SYSTEM_ARCHITECTURE.md. */
public final class DemoData {

    private DemoData() {
    }

    public static void seed(Services services) {
        RoomService rooms = services.rooms();
        DeviceService devices = services.devices();
        services.homes().createHome("Demo Home", Ecosystem.GOOGLE);

        Room living = rooms.createRoom("Living Room", RoomType.LIVING_ROOM);
        Room bedroom = rooms.createRoom("Bedroom", RoomType.BEDROOM);
        Room kitchen = rooms.createRoom("Kitchen", RoomType.KITCHEN);
        Room garage = rooms.createRoom("Garage", RoomType.GARAGE);

        Device livingLight = devices.addDevice(living.getId(), DeviceType.LIGHT, "Living Room Light");
        devices.addDevice(living.getId(), DeviceType.TV, "Living Room TV");
        devices.addDevice(living.getId(), DeviceType.THERMOSTAT, "Main Thermostat");
        devices.addDevice(living.getId(), DeviceType.FAN, "Ceiling Fan");
        Device bedroomLight = devices.addDevice(bedroom.getId(), DeviceType.LIGHT, "Bedroom Light");
        Device kitchenLight = devices.addDevice(kitchen.getId(), DeviceType.LIGHT, "Kitchen Light");
        devices.addDevice(garage.getId(), DeviceType.CAMERA, "Garage Camera");
        devices.addDevice(garage.getId(), DeviceType.DOOR_LOCK, "Front Door Lock");

        var automations = services.automations();
        automations.createScenario(AutomationScenarioBuilder.scenario("Good Night")
                .description("Everything off, 19 C, security armed")
                .at("22:30")
                .lighting(0)
                .turnOffAll(DeviceType.TV)
                .temperature(19)
                .armSecurity()
                .notifyWith("Good night! Doors are locked and cameras are recording."));

        automations.createScenario(AutomationScenarioBuilder.scenario("Good Morning")
                .description("Security disarmed, wake-up lights, a warmer house")
                .at("07:00")
                .disarmSecurity()
                .turnOn(bedroomLight.getId())
                .turnOn(kitchenLight.getId())
                .temperature(21));

        AutomationScenario movie = automations.createScenario(AutomationScenarioBuilder.scenario("Movie Mode")
                .description("Dimmed lights, TV on, door locked")
                .brightness(livingLight.getId(), 20)
                .turnOnAll(DeviceType.TV)
                .temperature(21)
                .lockAllDoors());

        // Prototype: a winter variant cloned from Movie Mode, then adjusted.
        AutomationScenario winter = automations.cloneScenario(movie.getId(), "Movie Mode Winter");
        winter.addAction(Action.forAll(DeviceType.THERMOSTAT, ActionType.SET_TEMPERATURE, 23.0));
        winter.addAction(Action.forAll(DeviceType.FAN, ActionType.TURN_OFF));
    }
}
