package com.smarthome.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Serves the web dashboard (the {@code web/} folder) for every path outside {@code /api}. */
public class StaticFileHandler implements HttpHandler {

    private static final Map<String, String> TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "js", "text/javascript; charset=utf-8",
            "json", "application/json; charset=utf-8",
            "svg", "image/svg+xml",
            "png", "image/png",
            "ico", "image/x-icon");

    private final Path root;

    public StaticFileHandler(Path root) {
        this.root = root == null ? null : root.toAbsolutePath().normalize();
    }

    /**
     * Finds the web folder: the {@code smarthome.web} system property, else {@code web/} in the working
     * directory, else {@code web/} next to the compiled classes (so it works from any directory).
     */
    public static Path locateWebRoot() {
        String configured = System.getProperty("smarthome.web");
        if (configured != null) {
            return Path.of(configured);
        }
        Path inWorkingDir = Path.of("web");
        if (Files.isDirectory(inWorkingDir)) {
            return inWorkingDir;
        }
        try {
            Path classes = Path.of(StaticFileHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path besideClasses = classes.getParent() == null ? null : classes.getParent().resolve("web");
            if (besideClasses != null && Files.isDirectory(besideClasses)) {
                return besideClasses;
            }
        } catch (URISyntaxException | SecurityException | NullPointerException e) {
            // fall through: dashboard unavailable, the API still works
        }
        return null;
    }

    public boolean isAvailable() {
        return root != null && Files.isRegularFile(root.resolve("index.html"));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if (!method.equals("GET") && !method.equals("HEAD")) {
            send(exchange, 405, "text/plain; charset=utf-8", "Method not allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }
        if (!isAvailable()) {
            send(exchange, 404, "text/plain; charset=utf-8",
                    "Dashboard files not found. The API is available at /api".getBytes(StandardCharsets.UTF_8));
            return;
        }
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/")) {
            path += "index.html";
        }
        Path file = root.resolve(path.substring(1)).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            send(exchange, 404, "text/plain; charset=utf-8", ("Not found: " + path).getBytes(StandardCharsets.UTF_8));
            return;
        }
        String name = file.getFileName().toString();
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
        send(exchange, 200, TYPES.getOrDefault(extension, "application/octet-stream"), Files.readAllBytes(file));
    }

    private static void send(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        boolean head = exchange.getRequestMethod().equalsIgnoreCase("HEAD");
        exchange.sendResponseHeaders(status, head ? -1 : body.length);
        if (!head) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
        exchange.close();
    }
}
