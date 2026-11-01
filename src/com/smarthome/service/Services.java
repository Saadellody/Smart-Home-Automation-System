package com.smarthome.service;

import com.smarthome.singleton.SmartHomeManager;

/** All application services, wired together around the single {@link SmartHomeManager}. */
public record Services(SmartHomeService homes, RoomService rooms, DeviceService devices,
                       AutomationService automations, NotificationService notifications) {

    public static Services create(SmartHomeManager manager) {
        NotificationService notifications = new NotificationService(manager);
        RoomService rooms = new RoomService(manager);
        return new Services(
                new SmartHomeService(manager, notifications),
                rooms,
                new DeviceService(manager, rooms, notifications),
                new AutomationService(manager, notifications),
                notifications);
    }
}
