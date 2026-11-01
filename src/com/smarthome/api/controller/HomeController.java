package com.smarthome.api.controller;

import com.smarthome.api.JsonBody;
import com.smarthome.api.JsonMapper;
import com.smarthome.api.Response;
import com.smarthome.api.Router;
import com.smarthome.model.SmartHome;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.Ecosystem;
import com.smarthome.model.enums.RoomType;
import com.smarthome.model.enums.TemperatureUnit;
import com.smarthome.model.enums.TriggerType;
import com.smarthome.service.SmartHomeService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeController {

    private final SmartHomeService smartHomeService;

    public HomeController(SmartHomeService smartHomeService) {
        this.smartHomeService = smartHomeService;
    }

    public void register(Router router) {
        router.get("/api/health", req -> Response.ok(Map.of("status", "UP")));
        router.get("/api/meta", req -> Response.ok(meta()));

        router.get("/api/home", req -> Response.ok(JsonMapper.home(smartHomeService.getHome())));
        router.post("/api/home", req -> {
            JsonBody body = req.body();
            SmartHome home = smartHomeService.createHome(body.requiredString("name"),
                    body.optEnum("ecosystem", Ecosystem.class));
            return Response.created(JsonMapper.home(home));
        });
        router.put("/api/home", req -> {
            JsonBody body = req.body();
            SmartHome home = smartHomeService.updateHome(body.optString("name"),
                    body.optEnum("ecosystem", Ecosystem.class));
            return Response.ok(JsonMapper.home(home));
        });
        router.get("/api/home/state", req -> Response.ok(Map.of("state", smartHomeService.describeState())));

        router.get("/api/home/configuration",
                req -> Response.ok(JsonMapper.configuration(smartHomeService.getConfiguration())));
        router.put("/api/home/configuration", req -> {
            JsonBody body = req.body();
            return Response.ok(JsonMapper.configuration(smartHomeService.updateConfiguration(
                    body.optEnum("temperatureUnit", TemperatureUnit.class),
                    body.optDouble("defaultTemperature"),
                    body.optBoolean("securityEnabled"),
                    body.optBoolean("notificationsEnabled"),
                    body.optBoolean("autoModeEnabled"))));
        });
    }

    /** Every accepted enum value, so a client can build its forms without hard-coding them. */
    private static Map<String, Object> meta() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("ecosystems", List.of(Ecosystem.values()));
        json.put("roomTypes", List.of(RoomType.values()));
        json.put("deviceTypes", List.of(DeviceType.values()));
        json.put("triggerTypes", List.of(TriggerType.values()));
        json.put("temperatureUnits", List.of(TemperatureUnit.values()));
        json.put("actionTypes", Arrays.stream(ActionType.values()).map(a -> {
            Map<String, Object> action = new LinkedHashMap<>();
            action.put("type", a);
            action.put("supportedDeviceType", a.getSupportedType() == null ? "ANY" : a.getSupportedType().name());
            action.put("requiresValue", a.requiresValue());
            return action;
        }).toList());
        return json;
    }
}
