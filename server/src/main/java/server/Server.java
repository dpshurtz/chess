package server;

import dataaccess.*;
import handler.AdminHandler;
import handler.ExceptionHandler;
import handler.GameHandler;
import handler.UserHandler;
import io.javalin.*;
import io.javalin.http.*;
import service.AdminService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {

        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        UserDAO userDAO = new MemoryUserDAO();

        AdminHandler adminHandler = new AdminHandler(new AdminService(authDAO, gameDAO, userDAO));
        GameHandler gameHandler = new GameHandler(new GameService(authDAO, gameDAO));
        UserHandler userHandler = new UserHandler(new UserService(authDAO, userDAO));
        ExceptionHandler exceptionHandler = new ExceptionHandler();

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", adminHandler::clear);
        javalin.post("/user", userHandler::register);
        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);
        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);

        javalin.exception(HttpResponseException.class, exceptionHandler::httpExceptionHandler);
        javalin.exception(DataAccessException.class, exceptionHandler::dataExceptionHandler);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
