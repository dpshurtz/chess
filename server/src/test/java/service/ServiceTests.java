package service;

import dataaccess.*;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serviceobjects.*;

import java.util.ArrayList;

class ServiceTests {
    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;
    private MemoryUserDAO userDAO;
    private AdminService adminService;
    private GameService gameService;
    private UserService userService;

    private String authToken;
    private final String badAuthToken = "badAuthToken";

    private final CreateGameRequest basicCreateGameRequest =
            new CreateGameRequest("gameName");
    private final JoinGameRequest basicJoinGameRequest =
            new JoinGameRequest("WHITE", 1);
    private final ListGamesRequest basicListGamesRequest =
            new ListGamesRequest();
    private final LoginRequest basicLoginRequest =
            new LoginRequest("username", "password");
    private final LogoutRequest basicLogoutRequest =
            new LogoutRequest();
    private final RegisterRequest basicRegisterRequest =
            new RegisterRequest("username", "password", "email");

    @BeforeEach
    public void resetAndRegister() throws DataAccessException {
        resetServiceAndDAO();
        authToken = userService.register(basicRegisterRequest).authToken();
    }

    @Test
    public void clear() throws DataAccessException {
        gameService.createGame(basicCreateGameRequest, authToken);

        adminService.clear();

        Assertions.assertTrue(authDAO.getAuthTable().isEmpty(),
                "AuthTable was not empty");
        Assertions.assertTrue(gameDAO.getGameTable().isEmpty(),
                "GameTable was not empty");
        Assertions.assertTrue(userDAO.getUserTable().isEmpty(),
                "UserTable was not empty");
    }

    @Test
    void listGames() throws DataAccessException {
        gameService.createGame(basicCreateGameRequest, authToken);

        ListGamesResult listGamesResult = gameService.listGames(basicListGamesRequest, authToken);

        var listGameTable = new ArrayList<ListGameData>();
        for (GameData game : gameDAO.getGameTable()) {
            listGameTable.add(new ListGameData(
                            game.gameID(),
                            game.whiteUsername(),
                            game.blackUsername(),
                            game.gameName()
                    )
            );
        }
        Assertions.assertEquals(listGameTable, listGamesResult.games(),
                "List does not match GameTable");
    }

    @Test
    void listGamesUnauthorized() throws DataAccessException {
        gameService.createGame(basicCreateGameRequest, authToken);

        Assertions.assertThrows(
                UnauthorizedResponse.class,
                () -> gameService.listGames(basicListGamesRequest, badAuthToken),
                "Unauthorized exception not thrown");
    }

    @Test
    void createGame() throws DataAccessException {
        gameService.createGame(basicCreateGameRequest, authToken);

        Assertions.assertFalse(gameDAO.getGameTable().isEmpty(),
                "GameTable is empty");
    }

    @Test
    void createGameDuplicateFails() throws DataAccessException {
        gameService.createGame(basicCreateGameRequest, authToken);

        Assertions.assertThrows(
                DataAccessException.class,
                () -> gameService.createGame(basicCreateGameRequest, authToken),
                "Data exception not thrown");
    }

    @Test
    void joinGame() throws DataAccessException {
        int gameID = gameService.createGame(basicCreateGameRequest, authToken).gameID();

        gameService.joinGame(basicJoinGameRequest, authToken);

        Assertions.assertEquals("username", gameDAO.getGame(gameID).whiteUsername(),
                "User was not added to game");
    }

    @Test
    void joinGameFailsWhenOccupied() throws DataAccessException {
        int gameID = gameService.createGame(basicCreateGameRequest, authToken).gameID();

        gameService.joinGame(basicJoinGameRequest, authToken);

        Assertions.assertThrows(
                ForbiddenResponse.class,
                () -> gameService.joinGame(basicJoinGameRequest, authToken),
                "Forbidden exception not thrown");
    }

    @Test
    void register() {
        Assertions.assertFalse(userDAO.getUserTable().isEmpty(),
                "UserTable is empty");
    }

    @Test
    void registerRequestIncomplete() {
        RegisterRequest badRegisterRequest =
                new RegisterRequest(null, "password", "email");

        Assertions.assertThrows(
                BadRequestResponse.class,
                () -> userService.register(badRegisterRequest),
                "BadRequest exception not thrown");
    }

    @Test
    void login() throws DataAccessException {
        userService.logout(basicLogoutRequest, authToken);

        authToken = userService.login(basicLoginRequest).authToken();

        Assertions.assertNotNull(authDAO.getAuth(authToken),
                "New AuthData not found");
    }

    @Test
    void loginWithWrongPassword() throws DataAccessException {
        userService.logout(basicLogoutRequest, authToken);
        LoginRequest badLoginRequest =
                new LoginRequest("username", "wrongPassword");

        Assertions.assertThrows(
                UnauthorizedResponse.class,
                () -> userService.login(badLoginRequest),
                "Unauthorized exception not thrown");
    }

    @Test
    void logout() throws DataAccessException {
        userService.logout(basicLogoutRequest, authToken);

        Assertions.assertNull(authDAO.getAuth(authToken),
                "Old AuthData still in table");
    }

    @Test
    void logoutUnauthorized() {
        Assertions.assertThrows(
                UnauthorizedResponse.class,
                () -> userService.logout(basicLogoutRequest, badAuthToken),
                "Unauthorized exception not thrown");
    }

    private void resetServiceAndDAO() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();
        adminService = new AdminService(authDAO, gameDAO, userDAO);
        gameService = new GameService(authDAO, gameDAO);
        userService = new UserService(authDAO, userDAO);
    }
}