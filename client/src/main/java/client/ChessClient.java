package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import serviceobjects.*;
import ui.EscapeSequences;
import ui.UIOption;
import client.ChessClient.ClientState;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class ChessClient {

    ServerFacade server;
    Scanner scanner;

    ClientState state;
    String authToken = null;
    ArrayList<ListGameData> availableGames;

    int currentGameID;
    ChessGame.TeamColor currentTeam;

    ArrayList<UIOption> preLoginOptions;
    ArrayList<UIOption> postLoginOptions;
    ArrayList<UIOption> inGameOptions;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        scanner = new Scanner(System.in);
        generatePreLoginOptions();
        generatePostLoginOptions();
        generateInGameOptions();
    }

    public enum ClientState {
        DONE,
        LOGGED_OUT,
        LOGGED_IN,
        IN_GAME
    }

    public void run() {
        state = ClientState.LOGGED_OUT;

        while (state != ClientState.DONE) {
            switch (state) {
                case LOGGED_OUT ->  executeMenuOptions(preLoginOptions);
                case LOGGED_IN ->   executeMenuOptions(postLoginOptions);
                case IN_GAME ->     executeMenuOptions(inGameOptions);
            }
        }
    }

    private void executeMenuOptions(ArrayList<UIOption> options) {
        System.out.println();
        int optionIndex = getMenuInput(options);
        System.out.println();

        if (0 <= optionIndex && optionIndex < options.size()) {
            options.get(optionIndex).action().run();
        }
        else {
            System.out.println("invalid");
        }
    }

    private int getMenuInput(ArrayList<UIOption> options) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println(i + " - " + options.get(i).name());
        }
        String line = scanner.nextLine();
        if (line.isBlank()) {
            return -1;
        }
        else {
            return Character.getNumericValue(line.charAt(0));
        }
    }

    private void generatePreLoginOptions() {
        preLoginOptions = new ArrayList<>();
        preLoginOptions.add(new UIOption(
                "login",
                "Logs you into the server",
                this::login));
        preLoginOptions.add(new UIOption(
                "register",
                "Creates you an account on the server",
                this::register));
        preLoginOptions.add(new UIOption(
                "quit",
                "Leaves this application",
                this::quit));
        preLoginOptions.add(new UIOption(
                "help",
                "Displays this information",
                this::helpPreLogin));
    }

    private void generatePostLoginOptions() {
        postLoginOptions = new ArrayList<>();
        postLoginOptions.add(new UIOption(
                "play game",
                "Lets you join a game that was listed",
                this::playGame));
        postLoginOptions.add(new UIOption(
                "create game",
                "Creates an empty game with no players",
                this::createGame));
        postLoginOptions.add(new UIOption(
                "list games",
                "Lists all current games",
                this::listGames));
        postLoginOptions.add(new UIOption(
                "observe game",
                "Lets you watch a game without joining",
                this::observeGame));
        postLoginOptions.add(new UIOption(
                "logout",
                "Returns to the login menu",
                this::logout));
        postLoginOptions.add(new UIOption(
                "help",
                "Displays this information",
                this::helpPostLogin));
    }

    private void generateInGameOptions() {
        inGameOptions = new ArrayList<>();
        inGameOptions.add(new UIOption(
                "redraw chessboard",
                "Displays the chessboard on the screen",
                this::redrawChessboard));
        inGameOptions.add(new UIOption(
                "make move",
                "Allows you to move a piece on your turn",
                this::makeMove));
        inGameOptions.add(new UIOption(
                "resign",
                "Forfeits the game to the opponent",
                this::resign));
        inGameOptions.add(new UIOption(
                "highlight legal moves",
                "Displays the board with legal moves from a specified location highlighted",
                this::highlightLegalMoves));
        inGameOptions.add(new UIOption(
                "leave game",
                "Returns to the main menu",
                this::leaveGame));
        inGameOptions.add(new UIOption(
                "help",
                "Displays this information",
                this::helpInGame));
    }

    private void login(){
        System.out.print("username >> ");
        String username = scanner.nextLine();
        System.out.print("password >> ");
        String password = scanner.nextLine();

        LoginResult result;
        try {
            result = server.login(new LoginRequest(username, password));
            authToken = result.authToken();
            state = ClientState.LOGGED_IN;
            System.out.println("Logged in as " + result.username());
        }
        catch (ResponseException e) {
            System.out.println("Login failed.");
            System.out.println(e.getMessage());
        }
    }

    private void register(){
        System.out.print("username >> ");
        String username = scanner.nextLine();
        System.out.print("password >> ");
        String password = scanner.nextLine();
        System.out.print("email >> ");
        String email = scanner.nextLine();

        RegisterResult result;

        try {
            result = server.register(new RegisterRequest(username, password, email));
            authToken = result.authToken();
            state = ClientState.LOGGED_IN;
            System.out.println("Registered and logged in as " + result.username());
        }
        catch (ResponseException e) {
            System.out.println("Registration failed.");
            System.out.println(e.getMessage());
        }
    }

    private void quit(){
        state = ClientState.DONE;
    }

    private void helpPreLogin(){
        for (UIOption options : preLoginOptions) {
            System.out.println(options.name() + " - " + options.description());
        }
    }

    private void playGame(){
        System.out.print("game number >> ");
        String line = scanner.nextLine();
        if (line.isBlank()) {
            System.out.println("invalid");
            return;
        }

        int gameIndex = Character.getNumericValue(line.charAt(0)) - 1;
        if (availableGames == null || 0 > gameIndex || gameIndex >= availableGames.size()) {
            System.out.println("invalid");
            return;
        }

        currentGameID = availableGames.get(gameIndex).gameID();

        System.out.print("join as white (w) or black (b) >> ");
        line = scanner.nextLine();
        if (line.isBlank()) {
            System.out.println("invalid");
            return;
        }

        char team = line.charAt(0);

        JoinGameRequest request;
        if (team == 'w') {
            currentTeam = ChessGame.TeamColor.WHITE;
            request = new JoinGameRequest("WHITE", currentGameID);
        }
        else if (team == 'b') {
            currentTeam = ChessGame.TeamColor.BLACK;
            request = new JoinGameRequest("BLACK", currentGameID);
        }
        else {
            System.out.println("invalid");
            return;
        }

        try {
            server.joinGame(request, authToken);
            displayGame(currentGameID, currentTeam);
            state = ClientState.IN_GAME;
        }
        catch (ResponseException e) {
            System.out.println("Failed to join game.");
            System.out.println(e.getMessage());
        }
    }

    private void createGame(){
        System.out.print("game name >> ");
        String gameName = scanner.nextLine();

        try {
            server.createGame(new CreateGameRequest(gameName), authToken);
            System.out.println("Game created.");
        }
        catch (ResponseException e) {
            System.out.println("Game creation failed.");
            System.out.println(e.getMessage());
        }
    }

    private void listGames(){
        ListGamesResult result;
        try {
            result = server.listGames(new ListGamesRequest(), authToken);
            availableGames = (ArrayList<ListGameData>) result.games();
            for (int i = 1; i <= result.games().size(); i++) {
                ListGameData game = availableGames.get(i - 1);
                System.out.println(i + " - " + game.gameName());
                System.out.println("\twhite: " + game.whiteUsername());
                System.out.println("\tblack: " + game.blackUsername());
            }
        }
        catch (ResponseException e) {
            System.out.println("Failed to fetch the games.");
            System.out.println(e.getMessage());
        }
    }

    private void observeGame(){
        System.out.print("game number >> ");
        String line = scanner.nextLine();
        if (line.isBlank()) {
            System.out.println("invalid");
            return;
        }

        int gameIndex = Character.getNumericValue(line.charAt(0)) - 1;
        if (availableGames == null || 0 > gameIndex || gameIndex >= availableGames.size()) {
            System.out.println("invalid");
            return;
        }

        currentGameID = availableGames.get(gameIndex).gameID();
        currentTeam = null;

        displayGame(currentGameID, ChessGame.TeamColor.WHITE);
        state = ClientState.IN_GAME;
    }

    private void logout(){
        try {
            server.logout(new LogoutRequest(), authToken);
            authToken = null;
            state = ClientState.LOGGED_OUT;
            System.out.println("Logged out.");
        }
        catch (ResponseException e) {
            System.out.println("Logout failed.");
            System.out.println(e.getMessage());
        }
    }

    private void helpPostLogin(){
        for (UIOption options : postLoginOptions) {
            System.out.println(options.name() + " - " + options.description());
        }
    }

    private void redrawChessboard() {
        displayGame(currentGameID, Objects.requireNonNullElse(currentTeam, ChessGame.TeamColor.WHITE));
    }

    private void makeMove() {

    }

    private void resign() {

    }

    private void highlightLegalMoves() {

    }

    private void leaveGame() {
        System.out.println("leaving game");
        state = ClientState.LOGGED_IN;
    }

    private void helpInGame() {
        for (UIOption options : inGameOptions) {
            System.out.println(options.name() + " - " + options.description());
        }
    }

    private void displayGame(int gameID, ChessGame.TeamColor team) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        int[] rows, cols;
        if (team == ChessGame.TeamColor.WHITE) {
            rows = new int[]{8, 7, 6, 5, 4, 3, 2, 1};
            cols = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        }
        else {
            rows = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
            cols = new int[]{8, 7, 6, 5, 4, 3, 2, 1};
        }

        for (int row : rows) {
            for (int col : cols) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));

                if ((row + col)%2 == 1) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                }
                else {
                    System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                }

                System.out.print(getPieceString(piece));
            }

            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        String pieceString;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            switch (piece.getPieceType()) {
                case KING -> pieceString = EscapeSequences.WHITE_KING;
                case QUEEN -> pieceString = EscapeSequences.WHITE_QUEEN;
                case BISHOP -> pieceString = EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> pieceString = EscapeSequences.WHITE_KNIGHT;
                case ROOK -> pieceString = EscapeSequences.WHITE_ROOK;
                case PAWN -> pieceString = EscapeSequences.WHITE_PAWN;
                default -> pieceString = EscapeSequences.EMPTY;
            }
            pieceString = EscapeSequences.SET_TEXT_COLOR_WHITE + pieceString;
        }
        else {
            switch (piece.getPieceType()) {
                case KING -> pieceString = EscapeSequences.BLACK_KING;
                case QUEEN -> pieceString = EscapeSequences.BLACK_QUEEN;
                case BISHOP -> pieceString = EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> pieceString = EscapeSequences.BLACK_KNIGHT;
                case ROOK -> pieceString = EscapeSequences.BLACK_ROOK;
                case PAWN -> pieceString = EscapeSequences.BLACK_PAWN;
                default -> pieceString = EscapeSequences.EMPTY;
            }
            pieceString = EscapeSequences.SET_TEXT_COLOR_BLACK + pieceString;
        }
        return pieceString;
    }
}
