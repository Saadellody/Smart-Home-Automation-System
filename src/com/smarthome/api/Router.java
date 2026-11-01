package com.smarthome.api;

import com.smarthome.exception.ResourceNotFoundException;
import com.smarthome.exception.SmartHomeException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps "METHOD /path/{param}" templates to handlers, turns exceptions into JSON error responses
 * and adds CORS headers so a browser front-end can call the API.
 */
public class Router implements HttpHandler {

    @FunctionalInterface
    public interface Handler {
        Response handle(Request request);
    }

    private record Route(String method, String template, Pattern pattern, List<String> params, Handler handler) {
    }

    private static final Pattern PARAM = Pattern.compile("\\{(\\w+)}");

    private final List<Route> routes = new ArrayList<>();

    public Router get(String template, Handler handler) {
        return add("GET", template, handler);
    }

    public Router post(String template, Handler handler) {
        return add("POST", template, handler);
    }

    public Router put(String template, Handler handler) {
        return add("PUT", template, handler);
    }

    public Router delete(String template, Handler handler) {
        return add("DELETE", template, handler);
    }

    private Router add(String method, String template, Handler handler) {
        List<String> params = new ArrayList<>();
        Matcher m = PARAM.matcher(template);
        StringBuilder regex = new StringBuilder("^");
        int last = 0;
        while (m.find()) {
            regex.append(Pattern.quote(template.substring(last, m.start()))).append("([^/]+)");
            params.add(m.group(1));
            last = m.end();
        }
        regex.append(Pattern.quote(template.substring(last))).append("/?$");
        routes.add(new Route(method, template, Pattern.compile(regex.toString()), params, handler));
        return this;
    }

    /** "METHOD template" of every registered route, for the index endpoint. */
    public List<String> describeRoutes() {
        return routes.stream().map(r -> r.method() + " " + r.template()).toList();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long start = System.nanoTime();
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();
        Response response;
        try {
            response = dispatch(exchange, method, path);
        } catch (BadRequestException | IllegalArgumentException e) {
            response = error(400, e.getMessage());
        } catch (ResourceNotFoundException e) {
            response = error(404, e.getMessage());
        } catch (SmartHomeException e) {
            response = error(422, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            response = error(500, "Internal server error");
        }
        send(exchange, response);
        System.out.printf("%s %s -> %d (%d ms)%n", method, path, response.status(), (System.nanoTime() - start) / 1_000_000);
    }

    private Response dispatch(HttpExchange exchange, String method, String path) throws IOException {
        if (method.equals("OPTIONS")) {
            return Response.noContent();
        }
        boolean pathMatched = false;
        for (Route route : routes) {
            Matcher m = route.pattern().matcher(path);
            if (!m.matches()) {
                continue;
            }
            pathMatched = true;
            if (!route.method().equals(method)) {
                continue;
            }
            Map<String, String> params = new LinkedHashMap<>();
            for (int i = 0; i < route.params().size(); i++) {
                params.put(route.params().get(i), m.group(i + 1));
            }
            Request request = new Request(method, path, params, exchange.getRequestURI().getRawQuery(), readBody(exchange));
            return route.handler().handle(request);
        }
        return pathMatched
                ? error(405, "Method " + method + " not allowed on " + path)
                : error(404, "No endpoint " + method + " " + path + " (see GET /api)");
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Response error(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 422 -> "Unprocessable Entity";
            default -> "Internal Server Error";
        });
        body.put("message", message);
        return new Response(status, body);
    }

    private static void send(HttpExchange exchange, Response response) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        if (response.body() == null) {
            exchange.sendResponseHeaders(response.status(), -1);
            exchange.close();
            return;
        }
        byte[] bytes = Json.write(response.body()).getBytes(StandardCharsets.UTF_8);
        headers.set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(response.status(), bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
