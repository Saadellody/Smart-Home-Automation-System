package com.smarthome.api;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** An incoming HTTP request after routing: path parameters, query parameters and the JSON body. */
public final class Request {

    private final String method;
    private final String path;
    private final Map<String, String> pathParams;
    private final Map<String, String> queryParams;
    private final String rawBody;
    private JsonBody body;

    Request(String method, String path, Map<String, String> pathParams, String rawQuery, String rawBody) {
        this.method = method;
        this.path = path;
        this.pathParams = pathParams;
        this.queryParams = parseQuery(rawQuery);
        this.rawBody = rawBody;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String pathParam(String name) {
        return pathParams.get(name);
    }

    public String query(String name) {
        String value = queryParams.get(name);
        return value == null || value.isBlank() ? null : value;
    }

    public <E extends Enum<E>> E queryEnum(String name, Class<E> type) {
        String value = query(name);
        return value == null ? null : JsonBody.parseEnum(value, type, name);
    }

    /** The JSON body as an object; an empty body is treated as {}. */
    public JsonBody body() {
        if (body == null) {
            if (rawBody == null || rawBody.isBlank()) {
                body = new JsonBody(new LinkedHashMap<>(), null);
            } else {
                try {
                    body = JsonBody.asBody(Json.parse(rawBody), "body");
                } catch (Json.JsonParseException e) {
                    throw new BadRequestException(e.getMessage());
                }
            }
        }
        return body;
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            String value = eq < 0 ? "" : pair.substring(eq + 1);
            params.put(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return params;
    }
}
