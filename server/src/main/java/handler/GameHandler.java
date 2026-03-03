package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import service.GameService;
import serviceobjects.*;

public class GameHandler {
    private final GameService gameService;
    private final Gson serializer = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx)
            throws UnauthorizedResponse {
        String authToken = ctx.header("Authorization");
        ListGamesRequest listGamesRequest = serializer.fromJson(ctx.body(), ListGamesRequest.class);

        ListGamesResult listGamesResult = gameService.listGames(listGamesRequest, authToken);
        ctx.result(serializer.toJson(listGamesResult));
        ctx.status(200);
    }

    public void createGame(Context ctx)
            throws UnauthorizedResponse, DataAccessException {
        String authToken = ctx.header("Authorization");
        CreateGameRequest createGameRequest = serializer.fromJson(ctx.body(), CreateGameRequest.class);

        CreateGameResult createGameResult = gameService.createGame(createGameRequest, authToken);
        ctx.result(serializer.toJson(createGameResult));
        ctx.status(200);
    }

    public void joinGame(Context ctx)
            throws UnauthorizedResponse, ForbiddenResponse, BadRequestResponse, DataAccessException {
        String authToken = ctx.header("Authorization");
        JoinGameRequest joinGameRequest = serializer.fromJson(ctx.body(), JoinGameRequest.class);

        gameService.joinGame(joinGameRequest, authToken);
        ctx.status(200);
    }
}
