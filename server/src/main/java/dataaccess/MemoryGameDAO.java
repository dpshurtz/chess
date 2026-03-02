package dataaccess;

import chess.ChessGame;
import model.GameData;
import serviceobjects.CreateGameResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MemoryGameDAO implements GameDAO{
    private Collection<GameData> gameTable = new HashSet<>();
    private int nextGameID = 1;

    @Override
    public CreateGameResult createGame(String gameName) throws DataAccessException {
        for (GameData game : gameTable) {
            if (Objects.equals(gameName, game.gameName())) {
                throw new DataAccessException("Game already exists");
            }
        }
        gameTable.add(new GameData(nextGameID, null, null, gameName, new ChessGame()));

        CreateGameResult result = new CreateGameResult(nextGameID);
        nextGameID++;
        return result;
    }

    @Override
    public GameData getGame(int gameID) {
        for (GameData game : gameTable) {
            if (gameID == game.gameID()) {
                return game;
            }
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return gameTable;
    }

    @Override
    public void updateGame(GameData newGameData) throws DataAccessException {
        if (!gameTable.remove(getGame(newGameData.gameID()))) {
            throw new DataAccessException("Corresponding game not found");
        }
        gameTable.add(newGameData);
    }

    @Override
    public void clear() {
        gameTable = new HashSet<>();
    }
}
