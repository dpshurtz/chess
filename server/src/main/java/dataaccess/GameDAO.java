package dataaccess;

import model.GameData;
import serviceobjects.CreateGameResult;
import serviceobjects.ListGameData;

import java.util.Collection;

public interface GameDAO {
    public CreateGameResult createGame(String gameName) throws DataAccessException;
    public GameData getGame(int gameID) throws DataAccessException;
    public Collection<ListGameData> listGames() throws DataAccessException;
    public void updateGame(GameData newGameData) throws DataAccessException;
    public void clear() throws DataAccessException;
}
