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
import java.util.Scanner;

public class ChessClient {

    ServerFacade server;
    Scanner scanner;

    ClientState state;
    String authToken = null;
    ArrayList<ListGameData> availableGames;

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
        options.get(optionIndex).action().run();
    }

    private int getMenuInput(ArrayList<UIOption> options) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println(i + " - " + options.get(i).name());
        }
        String line = scanner.nextLine();
        return Character.getNumericValue(line.charAt(0));
    }

    private void generatePreLoginOptions() {
        preLoginOptions = new ArrayList<>();
        preLoginOptions.add(new UIOption("login", "description", this::login));
        preLoginOptions.add(new UIOption("register", "description", this::register));
        preLoginOptions.add(new UIOption("quit", "description", this::quit));
        preLoginOptions.add(new UIOption("help", "description", this::helpPreLogin));
    }

    private void generatePostLoginOptions() {
        postLoginOptions = new ArrayList<>();
        postLoginOptions.add(new UIOption("play game", "description", this::playGame));
        postLoginOptions.add(new UIOption("create game", "description", this::createGame));
        postLoginOptions.add(new UIOption("list games", "description", this::listGames));
        postLoginOptions.add(new UIOption("observe game", "description", this::observeGame));
        postLoginOptions.add(new UIOption("logout", "description", this::logout));
        postLoginOptions.add(new UIOption("help", "description", this::helpPostLogin));
    }

    private void generateInGameOptions() {
        inGameOptions = new ArrayList<>();
        inGameOptions.add(new UIOption("leave game", "description", this::leaveGame));
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
        int gameIndex = Character.getNumericValue(scanner.nextLine().charAt(0));
        int gameID = availableGames.get(gameIndex).gameID();
        System.out.print("join as white (w) or black (b) >> ");
        char team = scanner.nextLine().charAt(0);

        JoinGameRequest request;
        if (team == 'w') {
            request = new JoinGameRequest("WHITE", gameID);
        }
        else {
            request = new JoinGameRequest("BLACK", gameID);
        }

        try {
            server.joinGame(request, authToken);
            displayGame(gameID);
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
            for (int i = 0; i < result.games().size(); i++) {
                ListGameData game = availableGames.get(i);
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
        int gameIndex = Character.getNumericValue(line.charAt(0));
        int gameID = availableGames.get(gameIndex).gameID();

        displayGame(gameID);
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

    private void leaveGame() {
        System.out.println("leaving game");
        state = ClientState.LOGGED_IN;
    }

    private void displayGame(int gameID) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        for (int row = 8; row >= 1; row--) {
            for (int col = 1; col <= 8; col++) {
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
