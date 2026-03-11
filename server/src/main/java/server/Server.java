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

/**
 * Creates a chess server using javalin
 */
public class Server {

    private final Javalin javalin;

    public Server() {
        try {
            // Initialize DAOs
            AuthDAO authDAO = new SQLAuthDAO();
            GameDAO gameDAO = new SQLGameDAO();
            UserDAO userDAO = new SQLUserDAO();

            // Initialize Handlers
            AdminHandler adminHandler = new AdminHandler(new AdminService(authDAO, gameDAO, userDAO));
            GameHandler gameHandler = new GameHandler(new GameService(authDAO, gameDAO));
            UserHandler userHandler = new UserHandler(new UserService(authDAO, userDAO));
            ExceptionHandler exceptionHandler = new ExceptionHandler();

            javalin = Javalin.create(config -> config.staticFiles.add("web"));

            // Register endpoints
            javalin.delete("/db", adminHandler::clear);
            javalin.post("/user", userHandler::register);
            javalin.post("/session", userHandler::login);
            javalin.delete("/session", userHandler::logout);
            javalin.get("/game", gameHandler::listGames);
            javalin.post("/game", gameHandler::createGame);
            javalin.put("/game", gameHandler::joinGame);

            // Register exception handlers
            javalin.exception(HttpResponseException.class, exceptionHandler::httpExceptionHandler);
            javalin.exception(DataAccessException.class, exceptionHandler::dataExceptionHandler);
        }
        catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Runs the chess server on a specified port
     * If the port given is 0, a random port is selected.
     *
     * @param desiredPort The port to run the server on
     * @return The port that the server is now running on
     */
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    /**
     * Stops the server
     */
    public void stop() {
        javalin.stop();
    }
}
