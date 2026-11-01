package com.smarthome.prototype;

import com.smarthome.builder.AutomationScenarioBuilder;
import com.smarthome.exception.ResourceNotFoundException;
import com.smarthome.model.Action;
import com.smarthome.model.AutomationScenario;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Prototype registry: keeps ready-made scenarios ("templates") and hands out independent copies.
 * <p>
 * Building Good Night or Movie Mode from zero in every home is repetitive. The built-in templates
 * target devices by type ("every light") rather than by id, so they work in any home.
 * Users can also save one of their own scenarios as a new template.
 */
public class AutomationPrototype {

    private final Map<String, AutomationScenario> prototypes = new LinkedHashMap<>();

    /** Stores a copy, so later changes to the given scenario do not change the template. */
    public void register(String key, AutomationScenario prototype) {
        if (prototype == null) {
            throw new IllegalArgumentException("Prototype is required");
        }
        prototypes.put(normalize(key), prototype.copy());
    }

    /** Returns a new, independent scenario cloned from the template. */
    public AutomationScenario create(String key) {
        AutomationScenario prototype = prototypes.get(normalize(key));
        if (prototype == null) {
            throw new ResourceNotFoundException("Template", key);
        }
        return prototype.copy();
    }

    public Map<String, AutomationScenario> getPrototypes() {
        return Collections.unmodifiableMap(prototypes);
    }

    private static String normalize(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Template key is required");
        }
        return key.trim().toLowerCase(Locale.ROOT).replace(' ', '-').replace('_', '-');
    }

    public static AutomationPrototype withDefaults() {
        AutomationPrototype registry = new AutomationPrototype();
        registry.register("good-night", AutomationScenarioBuilder.scenario("Good Night")
                .id("template-good-night")
                .description("Lights and TVs off, 19 C, security armed")
                .at("22:30")
                .lighting(0)
                .turnOffAll(DeviceType.TV)
                .temperature(19)
                .armSecurity()
                .notifyWith("Good night! Doors are locked and cameras are recording.")
                .build());
        registry.register("good-morning", AutomationScenarioBuilder.scenario("Good Morning")
                .id("template-good-morning")
                .description("Security disarmed, lights on, 21 C")
                .at("07:00")
                .disarmSecurity()
                .lighting(100)
                .temperature(21)
                .build());
        registry.register("movie-mode", AutomationScenarioBuilder.scenario("Movie Mode")
                .id("template-movie-mode")
                .description("Lights at 20%, TV on, 21 C, doors locked")
                .lighting(20)
                .turnOnAll(DeviceType.TV)
                .temperature(21)
                .lockAllDoors()
                .build());
        registry.register("away", AutomationScenarioBuilder.scenario("Away")
                .id("template-away")
                .description("Everything off, eco temperature, security armed")
                .onEvent("left-home")
                .lighting(0)
                .turnOffAll(DeviceType.TV)
                .turnOffAll(DeviceType.FAN)
                .temperature(17)
                .armSecurity()
                .notifyWith("You left home: the house is secured.")
                .build());
        registry.register("welcome-home", AutomationScenarioBuilder.scenario("Welcome Home")
                .id("template-welcome-home")
                .description("Security disarmed, front doors unlocked, lights on")
                .onEvent("arrived-home")
                .disarmSecurity()
                .addAction(Action.forAll(DeviceType.DOOR_LOCK, ActionType.UNLOCK))
                .lighting(80)
                .temperature(21)
                .build());
        return registry;
    }
}
