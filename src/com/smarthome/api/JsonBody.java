package com.smarthome.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Typed, validating access to a JSON object coming from a request. */
public final class JsonBody {

    private final Map<String, Object> values;
    private final String path;

    JsonBody(Map<String, Object> values, String path) {
        this.values = values;
        this.path = path;
    }

    public boolean has(String key) {
        return values.get(key) != null;
    }

    public String requiredString(String key) {
        String value = optString(key);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("'" + field(key) + "' is required");
        }
        return value;
    }

    public String optString(String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String s)) {
            throw new BadRequestException("'" + field(key) + "' must be a string");
        }
        return s;
    }

    public Double optDouble(String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Double d)) {
            throw new BadRequestException("'" + field(key) + "' must be a number");
        }
        return d;
    }

    public Boolean optBoolean(String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Boolean b)) {
            throw new BadRequestException("'" + field(key) + "' must be true or false");
        }
        return b;
    }

    public <E extends Enum<E>> E requiredEnum(String key, Class<E> type) {
        E value = optEnum(key, type);
        if (value == null) {
            throw new BadRequestException("'" + field(key) + "' is required, one of " + List.of(type.getEnumConstants()));
        }
        return value;
    }

    public <E extends Enum<E>> E optEnum(String key, Class<E> type) {
        String raw = optString(key);
        return raw == null ? null : parseEnum(raw, type, field(key));
    }

    public JsonBody optObject(String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        return asBody(value, field(key));
    }

    public List<JsonBody> optObjectList(String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> list)) {
            throw new BadRequestException("'" + field(key) + "' must be an array");
        }
        List<JsonBody> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(asBody(list.get(i), field(key) + "[" + i + "]"));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    static JsonBody asBody(Object value, String path) {
        if (!(value instanceof Map<?, ?>)) {
            throw new BadRequestException("'" + path + "' must be a JSON object");
        }
        return new JsonBody((Map<String, Object>) value, path);
    }

    /** Case-insensitive enum lookup that also accepts dashes or spaces ("door-lock" -> DOOR_LOCK). */
    public static <E extends Enum<E>> E parseEnum(String raw, Class<E> type, String field) {
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try {
            return Enum.valueOf(type, normalized);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid value '" + raw + "' for '" + field + "', expected one of "
                    + List.of(type.getEnumConstants()));
        }
    }

    private String field(String key) {
        return path == null ? key : path + "." + key;
    }
}
