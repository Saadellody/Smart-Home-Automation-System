package com.smarthome.model;

import com.smarthome.model.enums.NotificationType;

import java.time.Instant;

/** A message for the home owner, recorded only while notifications are enabled. */
public record Notification(String id, Instant time, NotificationType type, String message) {
}
