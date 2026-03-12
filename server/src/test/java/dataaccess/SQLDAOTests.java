package dataaccess;

import chess.ChessGame;
import io.javalin.http.UnauthorizedResponse;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serviceobjects.CreateGameResult;
import serviceobjects.ListGameData;

import java.util.Collection;

public class SQLDAOTests {

    private final SQLAuthDAO sqlAuthDAO = new SQLAuthDAO();
    private SQLGameDAO sqlGameDAO = new SQLGameDAO();
    private final SQLUserDAO sqlUserDAO = new SQLUserDAO();

    private final MemoryAuthDAO memoryAuthDAO = new MemoryAuthDAO();
    private MemoryGameDAO memoryGameDAO = new MemoryGameDAO();
    private final MemoryUserDAO memoryUserDAO = new MemoryUserDAO();

    private final String username = "username";
    private final String authToken = "authToken";
    private final String password = "password";
    private final String email = "email";
    private final String gameName = "gameName";

    private final AuthData defaultAuth = new AuthData(authToken, username);
    private final GameData defaultGame = new GameData(1, username, username, gameName, new ChessGame());
    private final UserData defaultUser = new UserData(username, password, email);

    private final AuthData badAuth = new AuthData(null, username);
    private final GameData badGame = new GameData(1, username, username, null, new ChessGame());
    private final UserData badUser = new UserData(null, password, email);

    public SQLDAOTests() throws DataAccessException {
    }


    @BeforeEach
    public void resetDB() throws DataAccessException {
        sqlAuthDAO.clear();
        sqlGameDAO.clear();
        sqlUserDAO.clear();
        memoryAuthDAO.clear();
        memoryGameDAO.clear();
        memoryUserDAO.clear();
    }

    @Test
    public void createAuth() throws DataAccessException {
        sqlAuthDAO.createAuth(defaultAuth);
        memoryAuthDAO.createAuth(defaultAuth);

        Assertions.assertEquals(memoryAuthDAO.getAuthTable(), sqlAuthDAO.getAuthTable(),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void createAuthBadRequest() throws DataAccessException {
        Assertions.assertThrows(
                DataAccessException.class,
                () -> sqlAuthDAO.createAuth(badAuth),
                "SQL Exception not thrown");
    }

    @Test
    public void getAuth() throws DataAccessException {
        sqlAuthDAO.createAuth(defaultAuth);

        Assertions.assertEquals(defaultAuth, sqlAuthDAO.getAuth(authToken),
                "Incorrect AuthData was returned");
    }

    @Test
    public void getAuthBadRequest() throws DataAccessException {
        Assertions.assertNull(sqlAuthDAO.getAuth(authToken),
                "AuthData from empty DB was not null");
    }

    @Test
    public void deleteAuth() throws DataAccessException {
        sqlAuthDAO.createAuth(defaultAuth);
        memoryAuthDAO.createAuth(defaultAuth);

        sqlAuthDAO.deleteAuth(authToken);
        memoryAuthDAO.deleteAuth(authToken);

        Assertions.assertEquals(memoryAuthDAO.getAuthTable(), sqlAuthDAO.getAuthTable(),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void deleteAuthBadRequest() throws DataAccessException {
        sqlAuthDAO.createAuth(defaultAuth);
        memoryAuthDAO.createAuth(defaultAuth);

        sqlAuthDAO.deleteAuth(authToken);

        Assertions.assertNotEquals(memoryAuthDAO.getAuthTable(), sqlAuthDAO.getAuthTable(),
                "SQL DAO gave same results as Memory DAO, but should not have");
    }

    @Test
    public void clearAuth() throws DataAccessException {
        sqlAuthDAO.createAuth(defaultAuth);
        memoryAuthDAO.createAuth(defaultAuth);

        sqlAuthDAO.clear();
        memoryAuthDAO.clear();

        Assertions.assertEquals(memoryAuthDAO.getAuthTable(), sqlAuthDAO.getAuthTable(),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void createGame() throws DataAccessException {
        sqlGameDAO.createGame(gameName);
        memoryGameDAO.createGame(gameName);

        Assertions.assertEquals(memoryGameDAO.getGameTable(), sqlGameDAO.getGameTable(),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void createGameBadRequest() throws DataAccessException {
        Assertions.assertThrows(
                DataAccessException.class,
                () -> sqlGameDAO.createGame(null),
                "SQL Exception not thrown");
    }

    @Test
    public void getGame() throws DataAccessException {
        sqlGameDAO.createGame(gameName);
        memoryGameDAO.createGame(gameName);

        Assertions.assertEquals(memoryGameDAO.getGame(1), sqlGameDAO.getGame(1),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void getGameBadRequest() throws DataAccessException {
        Assertions.assertNull(sqlGameDAO.getGame(1),
                "GameData from empty DB was not null");
    }

    @Test
    public void listGames() throws DataAccessException {
        sqlGameDAO.createGame(gameName);
        memoryGameDAO.createGame(gameName);

        Assertions.assertEquals(memoryGameDAO.listGames(), sqlGameDAO.listGames(),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void listGamesBadRequest() throws DataAccessException {
        sqlGameDAO.createGame(gameName);
        memoryGameDAO.createGame(gameName);

        sqlGameDAO = new SQLGameDAO();
        memoryGameDAO = new MemoryGameDAO();

        Assertions.assertNotEquals(memoryGameDAO.listGames(), sqlGameDAO.listGames(),
                "SQL DAO gave same results as Memory DAO but should not have");
    }

    @Test
    public void updateGame() throws DataAccessException {
        sqlGameDAO.createGame(gameName);
        memoryGameDAO.createGame(gameName);

        sqlGameDAO.updateGame(defaultGame);
        memoryGameDAO.updateGame(defaultGame);

        Assertions.assertEquals(memoryGameDAO.getGameTable(), sqlGameDAO.getGameTable(),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void updateGameBadRequest() throws DataAccessException {
        sqlGameDAO.createGame(gameName);

        Assertions.assertThrows(
                DataAccessException.class,
                () -> sqlGameDAO.updateGame(badGame),
                "SQL Exception not thrown");
    }

    @Test
    public void clearGame() throws DataAccessException {
        sqlGameDAO.createGame(gameName);
        memoryGameDAO.createGame(gameName);

        sqlGameDAO.clear();
        memoryGameDAO.clear();

        Assertions.assertEquals(memoryGameDAO.getGameTable(), sqlGameDAO.getGameTable(),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void createUser() throws DataAccessException {
        sqlUserDAO.createUser(defaultUser);
        memoryUserDAO.createUser(defaultUser);

        Assertions.assertEquals(memoryUserDAO.getUserTable(), sqlUserDAO.getUserTable(),
                "SQL DAO did not give same results as Memory DAO");
    }

    @Test
    public void createUserBadRequest() throws DataAccessException {
        Assertions.assertThrows(
                DataAccessException.class,
                () -> sqlUserDAO.createUser(badUser),
                "SQL Exception not thrown");
    }

    @Test
    public void getUser() throws DataAccessException {
        sqlUserDAO.createUser(defaultUser);

        Assertions.assertEquals(defaultUser, sqlUserDAO.getUser(username),
                "Incorrect UserData was returned");
    }

    @Test
    public void getUserBadRequest() throws DataAccessException {
        Assertions.assertNull(sqlUserDAO.getUser(username),
                "UserData from empty DB was not null");
    }

    @Test
    public void clearUser() throws DataAccessException {
        sqlUserDAO.createUser(defaultUser);
        memoryUserDAO.createUser(defaultUser);

        sqlUserDAO.clear();
        memoryUserDAO.clear();

        Assertions.assertEquals(memoryUserDAO.getUserTable(), sqlUserDAO.getUserTable(),
                "SQL DAO did not give same results as Memory DAO");
    }
}
