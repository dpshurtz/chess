package dataaccess;

import chess.ChessGame;
import model.GameData;
import serviceobjects.CreateGameResult;
import serviceobjects.ListGameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class MemoryGameDAO implements GameDAO{
    private Collection<GameData> gameTable = new ArrayList<>();
    private int nextGameID = 1;

    @Override
    public CreateGameResult createGame(String gameName) throws DataAccessException {
        for (GameData game : gameTable) {
            if (Objects.equals(gameName, game.gameName())) {
                throw new DataAccessException("Error: Game already exists");
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
    public Collection<ListGameData> listGames() {
        var result = new ArrayList<ListGameData>();
        for (GameData game : gameTable) {
            result.add(new ListGameData(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName()
                )
            );
        }
        return result;
    }

    @Override
    public void updateGame(GameData newGameData) throws DataAccessException {
        if (!gameTable.remove(getGame(newGameData.gameID()))) {
            throw new DataAccessException("Error: Corresponding game not found");
        }
        gameTable.add(newGameData);
    }

    @Override
    public void clear() {
        gameTable = new ArrayList<>();
    }

    public Collection<GameData> getGameTable() {
        return gameTable;
    }
}
