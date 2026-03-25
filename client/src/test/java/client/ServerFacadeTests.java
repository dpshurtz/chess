package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import server.Server;
import serviceobjects.*;

import java.util.ArrayList;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

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

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() throws ResponseException {
        facade.clear();
        server.stop();
    }

    @BeforeEach
    public void resetDB() throws ResponseException {
        facade.clear();
        authToken = facade.register(basicRegisterRequest).authToken();
    }

    @Test
    public void clear() throws ResponseException {
        facade.createGame(basicCreateGameRequest, authToken);

        facade.clear();
        authToken = facade.register(basicRegisterRequest).authToken();

        Assertions.assertTrue(facade.listGames(basicListGamesRequest, authToken).games().isEmpty(),
                "GameTable was not empty");
    }

    @Test
    void listGames() throws ResponseException {
        facade.createGame(basicCreateGameRequest, authToken);

        ListGamesResult listGamesResult = facade.listGames(basicListGamesRequest, authToken);

        var listGameTable = new ArrayList<ListGameData>();
        for (ListGameData game : facade.listGames(basicListGamesRequest, authToken).games()) {
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
    void listGamesUnauthorized() throws ResponseException {
        facade.createGame(basicCreateGameRequest, authToken);

        Assertions.assertThrows(
                ResponseException.class,
                () -> facade.listGames(basicListGamesRequest, badAuthToken),
                "Unauthorized exception not thrown");
    }

    @Test
    void createGame() throws ResponseException {
        facade.createGame(basicCreateGameRequest, authToken);

        Assertions.assertFalse(facade.listGames(basicListGamesRequest, authToken).games().isEmpty(),
                "GameTable is empty");
    }

    @Test
    void createGameDuplicateFails() throws ResponseException {
        facade.createGame(basicCreateGameRequest, authToken);

        Assertions.assertThrows(
                ResponseException.class,
                () -> facade.createGame(basicCreateGameRequest, authToken),
                "Data exception not thrown");
    }

    @Test
    void joinGame() throws ResponseException {
        int gameID = facade.createGame(basicCreateGameRequest, authToken).gameID();

        facade.joinGame(basicJoinGameRequest, authToken);

        String addedUser = null;
        var games = facade.listGames(basicListGamesRequest, authToken).games();
        for (var game : games) {
            if (game.gameID() == gameID) {
                addedUser = game.whiteUsername();
            }
        }
        Assertions.assertEquals("username", addedUser,
                "User was not added to game");
    }

    @Test
    void joinGameFailsWhenOccupied() throws ResponseException {
        int gameID = facade.createGame(basicCreateGameRequest, authToken).gameID();

        facade.joinGame(basicJoinGameRequest, authToken);

        Assertions.assertThrows(
                ResponseException.class,
                () -> facade.joinGame(basicJoinGameRequest, authToken),
                "Forbidden exception not thrown");
    }

    @Test
    void register() {
        Assertions.assertDoesNotThrow(
                () -> facade.logout(basicLogoutRequest, authToken),
                "Unable to logout");
    }

    @Test
    void registerRequestIncomplete() {
        RegisterRequest badRegisterRequest =
                new RegisterRequest(null, "password", "email");

        Assertions.assertThrows(
                ResponseException.class,
                () -> facade.register(badRegisterRequest),
                "BadRequest exception not thrown");
    }

    @Test
    void login() throws ResponseException {
        facade.logout(basicLogoutRequest, authToken);

        authToken = facade.login(basicLoginRequest).authToken();

        Assertions.assertDoesNotThrow(
                () -> facade.logout(basicLogoutRequest, authToken),
                "Unable to logout");
    }

    @Test
    void loginWithWrongPassword() throws ResponseException {
        facade.logout(basicLogoutRequest, authToken);
        LoginRequest badLoginRequest =
                new LoginRequest("username", "wrongPassword");

        Assertions.assertThrows(
                ResponseException.class,
                () -> facade.login(badLoginRequest),
                "Unauthorized exception not thrown");
    }

    @Test
    void logout() throws ResponseException {
        facade.logout(basicLogoutRequest, authToken);

        Assertions.assertDoesNotThrow(
                () -> facade.login(basicLoginRequest),
                "Unable to log back in");
    }

    @Test
    void logoutUnauthorized() {
        Assertions.assertThrows(
                ResponseException.class,
                () -> facade.logout(basicLogoutRequest, badAuthToken),
                "Unauthorized exception not thrown");
    }

}
