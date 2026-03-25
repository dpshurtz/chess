package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import serviceobjects.CreateGameResult;
import serviceobjects.ListGameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO{

    String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game (
                `gameID` int NOT NULL AUTO_INCREMENT,
                `whiteUsername` varchar(256),
                `blackUsername` varchar(256),
                `gameName` varchar(256) NOT NULL UNIQUE,
                `game` TEXT,
                PRIMARY KEY (`gameID`),
                INDEX(gameName)
            )
            """
    };

    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public CreateGameResult createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO game (gameName, game) VALUES (?, ?)";
        int gameID = DatabaseManager.executeUpdate(statement, gameName, new ChessGame());
        return new CreateGameResult(gameID);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                new Gson().fromJson(rs.getString("game"), ChessGame.class)
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: Unable to read data: %s", e.getMessage()), e);
        }
        return null;
    }

    @Override
    public Collection<ListGameData> listGames() throws DataAccessException {
        var result = new ArrayList<ListGameData>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new ListGameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName")
                                )
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: Unable to read data: %s", e.getMessage()), e);
        }
        return result;

    }

    @Override
    public void updateGame(GameData newGameData) throws DataAccessException {
        var statement = "DELETE FROM game WHERE gameID=?";
        DatabaseManager.executeUpdate(statement, newGameData.gameID());

        statement =
                "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) " +
                "VALUES (?, ?, ?, ?, ?)";
        DatabaseManager.executeUpdate(statement,
                newGameData.gameID(),
                newGameData.whiteUsername(),
                newGameData.blackUsername(),
                newGameData.gameName(),
                newGameData.game()
        );
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE game";
        DatabaseManager.executeUpdate(statement);
    }

    public Collection<GameData> getGameTable() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new GameData(
                                        rs.getInt("gameID"),
                                        rs.getString("whiteUsername"),
                                        rs.getString("blackUsername"),
                                        rs.getString("gameName"),
                                        new Gson().fromJson(rs.getString("game"), ChessGame.class)
                                )
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: Unable to read data: %s", e.getMessage()), e);
        }
        return result;
    }
}
