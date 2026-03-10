package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SQLAuthDAO implements AuthDAO{

    String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
                `id` int NOT NULL AUTO_INCREMENT,
                `authToken` varchar(256) NOT NULL,
                `username` varchar(256) NOT NULL,
                PRIMARY KEY (`id`),
                INDEX(authToken)
            )
            """
    };

    public SQLAuthDAO() throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        DatabaseManager.executeUpdate(statement, authData.authToken(), authData.username());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(
                                rs.getString("authToken"),
                                rs.getString("username")
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()), e);
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken=?";
        DatabaseManager.executeUpdate(statement, authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE auth";
        DatabaseManager.executeUpdate(statement);
    }
}
