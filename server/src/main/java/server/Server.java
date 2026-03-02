package server;

import io.javalin.Javalin;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Clear application
        javalin.delete("/db", ctx -> {
        });

        // Register
        javalin.post("/user", ctx -> {
        });

        // Login
        javalin.post("/session", ctx -> {
        });

        // Logout
        javalin.delete("/session", ctx -> {
        });

        // List games
        javalin.get("/game", ctx -> {
        });

        // Create game
        javalin.post("/game", ctx -> {
        });

        // Join game
        javalin.put("/game", ctx -> {
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
