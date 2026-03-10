package dataaccess;

import model.GameData;
import serviceobjects.CreateGameResult;

import java.util.Collection;
import java.util.List;

public class SQLGameDAO implements GameDAO{

    String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game (
                `id` int NOT NULL AUTO_INCREMENT,
                `whiteUsername` varchar(256),
                `blackUsername` varchar(256),
                `gameName` varchar(256) NOT NULL,
                `game` TEXT,
                PRIMARY KEY (`id`),
                INDEX(gameName)
            )
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
