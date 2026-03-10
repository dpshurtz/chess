package dataaccess;

import model.GameData;
import serviceobjects.CreateGameResult;

import java.util.Collection;
import java.util.List;

public class SQLGameDAO implements GameDAO{

    String[] createStatements = {
            """
            """
    };

    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public CreateGameResult createGame(String gameName) throws DataAccessException {
        return null;
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

    }
}
