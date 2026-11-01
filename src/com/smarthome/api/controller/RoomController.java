package com.smarthome.api.controller;

import com.smarthome.api.JsonBody;
import com.smarthome.api.JsonMapper;
import com.smarthome.api.Response;
import com.smarthome.api.Router;
import com.smarthome.model.enums.DeviceType;
import com.smarthome.model.enums.RoomType;
import com.smarthome.service.DeviceService;
import com.smarthome.service.RoomService;

public class RoomController {

    private final RoomService roomService;
    private final DeviceService deviceService;

    public RoomController(RoomService roomService, DeviceService deviceService) {
        this.roomService = roomService;
        this.deviceService = deviceService;
    }

    public void register(Router router) {
        router.get("/api/rooms", req -> Response.ok(JsonMapper.rooms(roomService.listRooms())));
        router.post("/api/rooms", req -> {
            JsonBody body = req.body();
            return Response.created(JsonMapper.room(roomService.createRoom(
                    body.requiredString("name"), body.requiredEnum("type", RoomType.class))));
        });
        router.get("/api/rooms/{id}", req -> Response.ok(JsonMapper.room(roomService.getRoom(req.pathParam("id")))));
        router.put("/api/rooms/{id}", req -> {
            JsonBody body = req.body();
            return Response.ok(JsonMapper.room(roomService.updateRoom(req.pathParam("id"),
                    body.optString("name"), body.optEnum("type", RoomType.class))));
        });
        router.delete("/api/rooms/{id}", req -> {
            roomService.deleteRoom(req.pathParam("id"));
            return Response.noContent();
        });

        router.get("/api/rooms/{id}/devices",
                req -> Response.ok(JsonMapper.devices(roomService.listDevices(req.pathParam("id")))));
        router.post("/api/rooms/{id}/devices", req -> {
            JsonBody body = req.body();
            return Response.created(JsonMapper.device(deviceService.addDevice(req.pathParam("id"),
                    body.requiredEnum("type", DeviceType.class), body.requiredString("name"))));
        });
    }
}
