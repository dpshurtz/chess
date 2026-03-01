package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MemoryGameDAO implements GameDAO{
    private Collection<GameData> gameTable = new HashSet<>();

    @Override
    public void createGame(String gameName) throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return List.of();
    }

    @Override
    public void updateGame(GameData newGameData) throws DataAccessException {

    }

    @Override
    public void clear() {
        gameTable = new HashSet<>();
    }
}
