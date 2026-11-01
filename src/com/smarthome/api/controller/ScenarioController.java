package com.smarthome.api.controller;

import com.smarthome.api.BadRequestException;
import com.smarthome.api.JsonBody;
import com.smarthome.api.JsonMapper;
import com.smarthome.api.Response;
import com.smarthome.api.Router;
import com.smarthome.builder.AutomationScenarioBuilder;
import com.smarthome.model.Action;
import com.smarthome.model.Trigger;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.TriggerType;
import com.smarthome.service.AutomationService;

import java.util.List;
import java.util.Map;

public class ScenarioController {

    private final AutomationService automationService;

    public ScenarioController(AutomationService automationService) {
        this.automationService = automationService;
    }

    public void register(Router router) {
        router.get("/api/scenarios", req -> Response.ok(JsonMapper.scenarios(automationService.listScenarios())));
        router.post("/api/scenarios", req -> {
            JsonBody body = req.body();
            List<Action> actions = parseActions(body);
            if (actions == null) {
                throw new BadRequestException("'actions' is required");
            }
            AutomationScenarioBuilder builder = AutomationScenarioBuilder.scenario(body.requiredString("name"))
                    .description(body.optString("description"))
                    .trigger(parseTrigger(body))
                    .addActions(actions);
            Boolean enabled = body.optBoolean("enabled");
            if (enabled != null) {
                builder.enabled(enabled);
            }
            return Response.created(JsonMapper.scenario(automationService.createScenario(builder)));
        });
        router.get("/api/scenarios/{id}",
                req -> Response.ok(JsonMapper.scenario(automationService.getScenario(req.pathParam("id")))));
        router.put("/api/scenarios/{id}", req -> {
            JsonBody body = req.body();
            return Response.ok(JsonMapper.scenario(automationService.updateScenario(req.pathParam("id"),
                    body.optString("name"), body.optString("description"), parseTrigger(body),
                    parseActions(body), body.optBoolean("enabled"))));
        });
        router.delete("/api/scenarios/{id}", req -> {
            automationService.deleteScenario(req.pathParam("id"));
            return Response.noContent();
        });

        router.post("/api/scenarios/{id}/execute",
                req -> Response.ok(JsonMapper.report(automationService.execute(req.pathParam("id")))));
        router.post("/api/scenarios/{id}/clone", req -> Response.created(JsonMapper.scenario(
                automationService.cloneScenario(req.pathParam("id"), req.body().optString("name")))));
        router.post("/api/scenarios/{id}/enable",
                req -> Response.ok(JsonMapper.scenario(automationService.enable(req.pathParam("id")))));
        router.post("/api/scenarios/{id}/disable",
                req -> Response.ok(JsonMapper.scenario(automationService.disable(req.pathParam("id")))));

        router.post("/api/events", req -> {
            String event = req.body().requiredString("name");
            List<Map<String, Object>> reports = automationService.fireEvent(event).stream()
                    .map(JsonMapper::report).toList();
            return Response.ok(Map.of("event", event, "executed", reports));
        });
    }

    private static Trigger parseTrigger(JsonBody body) {
        JsonBody trigger = body.optObject("trigger");
        if (trigger == null) {
            return null;
        }
        return new Trigger(trigger.requiredEnum("type", TriggerType.class), trigger.optString("value"));
    }

    private static List<Action> parseActions(JsonBody body) {
        List<JsonBody> actions = body.optObjectList("actions");
        if (actions == null) {
            return null;
        }
        return actions.stream().map(a -> new Action(
                a.requiredEnum("type", ActionType.class),
                a.optString("deviceId"),
                a.optEnum("deviceType", DeviceType.class),
                a.optString("roomId"),
                a.optDouble("value"))).toList();
    }
}
