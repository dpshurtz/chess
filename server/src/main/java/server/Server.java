package server;

import handler.AdminHandler;
import handler.GameHandler;
import handler.UserHandler;
import io.javalin.*;

public class Server {

    private final Javalin javalin;

    private final AdminHandler adminHandler = new AdminHandler();
    private final GameHandler gameHandler = new GameHandler();
    private final UserHandler userHandler = new UserHandler();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", adminHandler::clear);
        javalin.post("/user", userHandler::register);
        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);
        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
