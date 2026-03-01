package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    public void createGame(String gameName) throws DataAccessException;
    public GameData getGame(int gameID);
    public Collection<GameData> listGames();
    public void updateGame(GameData newGameData) throws DataAccessException;
    public void clear();
}
