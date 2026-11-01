package com.smarthome.api.controller;

import com.smarthome.api.JsonBody;
import com.smarthome.api.JsonMapper;
import com.smarthome.api.Response;
import com.smarthome.api.Router;
import com.smarthome.model.Device;
import com.smarthome.model.enums.ActionType;
import com.smarthome.model.enums.DeviceStatus;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.service.DeviceService;

public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public void register(Router router) {
        router.get("/api/devices", req -> Response.ok(JsonMapper.devices(deviceService.listDevices(
                req.queryEnum("type", DeviceType.class),
                req.query("roomId"),
                req.queryEnum("status", DeviceStatus.class)))));
        router.post("/api/devices", req -> {
            JsonBody body = req.body();
            return Response.created(JsonMapper.device(deviceService.addDevice(body.requiredString("roomId"),
                    body.requiredEnum("type", DeviceType.class), body.requiredString("name"))));
        });
        router.get("/api/devices/{id}", req -> Response.ok(JsonMapper.device(deviceService.getDevice(req.pathParam("id")))));
        router.put("/api/devices/{id}", req -> {
            String id = req.pathParam("id");
            JsonBody body = req.body();
            Device device = deviceService.getDevice(id);
            if (body.has("roomId")) {
                device = deviceService.moveDevice(id, body.optString("roomId"));
            }
            if (body.has("name")) {
                device = deviceService.renameDevice(id, body.optString("name"));
            }
            return Response.ok(JsonMapper.device(device));
        });
        router.delete("/api/devices/{id}", req -> {
            deviceService.removeDevice(req.pathParam("id"));
            return Response.noContent();
        });

        router.post("/api/devices/{id}/on", req -> Response.ok(JsonMapper.device(deviceService.turnOn(req.pathParam("id")))));
        router.post("/api/devices/{id}/off", req -> Response.ok(JsonMapper.device(deviceService.turnOff(req.pathParam("id")))));
        router.post("/api/devices/{id}/commands", req -> {
            JsonBody body = req.body();
            ActionType type = body.requiredEnum("type", ActionType.class);
            return Response.ok(JsonMapper.device(deviceService.executeCommand(req.pathParam("id"),
                    type, JsonMapper.toDomainValue(type, body.optDouble("value")))));
        });
    }
}
