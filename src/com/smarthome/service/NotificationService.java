package com.smarthome.service;

import com.smarthome.model.Notification;
import com.smarthome.model.SmartHome;
import com.smarthome.model.enums.NotificationType;
import com.smarthome.singleton.SmartHomeManager;
import com.smarthome.util.IdGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/** Records notifications on the home, only while notifications are enabled in its configuration. */
public class NotificationService {

    private final SmartHomeManager manager;
    private final Clock clock;

    public NotificationService(SmartHomeManager manager) {
        this(manager, Clock.systemUTC());
    }

    public NotificationService(SmartHomeManager manager, Clock clock) {
        this.manager = manager;
        this.clock = clock;
    }

    /** Returns the notification, or null when notifications are disabled. */
    public Notification notify(NotificationType type, String message) {
        SmartHome home = manager.getHome();
        if (!home.getConfiguration().isNotificationsEnabled()) {
            return null;
        }
        Notification notification = new Notification(IdGenerator.next("ntf"), Instant.now(clock), type, message);
        home.addNotification(notification);
        return notification;
    }

    /** Newest first. */
    public List<Notification> list() {
        return manager.getHome().getNotifications();
    }

    public void clear() {
        manager.getHome().clearNotifications();
    }
}
