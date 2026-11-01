package com.smarthome.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Generates readable, prefixed identifiers such as "room-1", "dev-4", "scn-2". */
public final class IdGenerator {

    private static final Map<String, AtomicLong> COUNTERS = new ConcurrentHashMap<>();

    private IdGenerator() {
    }

    public static String next(String prefix) {
        return prefix + "-" + COUNTERS.computeIfAbsent(prefix, p -> new AtomicLong()).incrementAndGet();
    }
}
