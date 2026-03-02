package handler;

import io.javalin.http.Context;
import service.GameService;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {

    }

    public void createGame(Context ctx) {

    }

    public void joinGame(Context ctx) {

    }
}
