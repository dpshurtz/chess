package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.GameService;
import serviceobjects.JoinGameRequest;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {

    }

    public void createGame(Context ctx) {

    }

    public void joinGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("Authorization");
        JoinGameRequest joinGameRequest = new Gson().fromJson(ctx.body(), JoinGameRequest.class);

        gameService.joinGame(joinGameRequest, authToken);
        ctx.status(200);
    }
}
