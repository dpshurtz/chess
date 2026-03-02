package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import model.AuthData;
import model.GameData;
import serviceobjects.*;

import java.util.Objects;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest, String authToken)
            throws UnauthorizedResponse {
        if (authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedResponse("unauthorized");
        }

        return new ListGamesResult(gameDAO.listGames());
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest, String authToken)
            throws UnauthorizedResponse, DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedResponse("unauthorized");
        }

        return gameDAO.createGame(createGameRequest.gameName());
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken)
            throws UnauthorizedResponse, ForbiddenResponse, BadRequestResponse, DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedResponse("unauthorized");
        }

        GameData game = gameDAO.getGame(joinGameRequest.gameID());
        GameData newGame;
        if (Objects.equals(joinGameRequest.playerColor(), "WHITE")) {
            if (game.whiteUsername() != null) {
                throw new ForbiddenResponse("already taken");
            }
            newGame = new GameData(
                    game.gameID(),
                    authData.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
        }
        else if (Objects.equals(joinGameRequest.playerColor(), "BLACK")) {
            if (game.blackUsername() != null) {
                throw new ForbiddenResponse("already taken");
            }
            newGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    authData.username(),
                    game.gameName(),
                    game.game()
            );
        }
        else {
            throw new BadRequestResponse("invalid color");
        }

        gameDAO.updateGame(newGame);
    }
}
