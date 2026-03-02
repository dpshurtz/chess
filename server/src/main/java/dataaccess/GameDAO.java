package dataaccess;

import model.GameData;
import serviceobjects.CreateGameResult;

import java.util.Collection;

public interface GameDAO {
    public CreateGameResult createGame(String gameName) throws DataAccessException;
    public GameData getGame(int gameID);
    public Collection<GameData> listGames();
    public void updateGame(GameData newGameData) throws DataAccessException;
    public void clear();
}
