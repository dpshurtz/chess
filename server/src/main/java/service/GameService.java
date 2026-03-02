package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import serviceobjects.*;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) {
        return null;
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) {
        return null;
    }

    public void joinGame(JoinGameRequest joinGameRequest) {

    }
}
