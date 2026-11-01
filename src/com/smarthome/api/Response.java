package com.smarthome.api;

/** Status code plus a body that is serialized to JSON (null = no body). */
public record Response(int status, Object body) {

    public static Response ok(Object body) {
        return new Response(200, body);
    }

    public static Response created(Object body) {
        return new Response(201, body);
    }

    public static Response noContent() {
        return new Response(204, null);
    }
}
