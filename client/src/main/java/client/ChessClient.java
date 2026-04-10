package client;

import chess.*;
import exception.ResponseException;
import serviceobjects.*;
import ui.EscapeSequences;
import ui.UIOption;
import websocket.ServerMessageHandler;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.util.*;

public class ChessClient implements ServerMessageHandler {

    private final ServerFacade server;
    private final WebSocketFacade ws;
    private final Scanner scanner;

    private ClientState state;
    private String authToken = null;
    private ArrayList<ListGameData> availableGames;

    private int currentGameID;
    private ChessGame.TeamColor currentTeam;
    private ChessBoard currentBoard = null;

    private ArrayList<UIOption> preLoginOptions;
    private ArrayList<UIOption> postLoginOptions;
    private ArrayList<UIOption> inGameOptions;

    public ChessClient(String serverUrl) throws ResponseException {
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, this);
        scanner = new Scanner(System.in);
        generatePreLoginOptions();
        generatePostLoginOptions();
        generateInGameOptions();
    }

    @Override
    public void notify(ServerMessage message) {
        System.out.println();
        switch (message.getServerMessageType()) {
            case NOTIFICATION ->
                    System.out.println(((NotificationMessage)message).getMessage());
            case ERROR ->
                    System.out.println(((ErrorMessage)message).getMessage());
            case LOAD_GAME -> {
                currentBoard = ((LoadGameMessage)message).getGameBoard();
                displayGame(currentTeam);
            }
            case VALID_MOVES -> {
                Collection<ChessMove> moves = ((ValidMovesMessage)message).getValidMoves();
                HashSet<ChessPosition> highlightPositions = new HashSet<>();
                ChessPosition origin = null;
                if (!moves.isEmpty()) {
                    origin = moves.iterator().next().getStartPosition();
                }

                for (ChessMove move : moves) {
                    highlightPositions.add(move.getEndPosition());
                }

                displayGame(currentTeam, origin, highlightPositions);
            }
        }

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
            ws.sendCommand(UserGameCommand.CommandType.CONNECT, authToken, currentGameID, currentTeam);

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

        try {
            ws.sendCommand(UserGameCommand.CommandType.CONNECT, authToken, currentGameID, null);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

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
        displayGame(Objects.requireNonNullElse(currentTeam, ChessGame.TeamColor.WHITE));
    }

    private void makeMove() {
        System.out.print("starting square >> ");
        ChessPosition startPosition = getPositionInput();
        if (startPosition == null) {
            System.out.println("invalid");
            return;
        }

        System.out.print("ending square >> ");
        ChessPosition endPosition = getPositionInput();
        if (endPosition == null) {
            System.out.println("invalid");
            return;
        }

        ChessPiece piece = currentBoard.getPiece(startPosition);
        ChessPiece.PieceType pieceType = null;
        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN
                && currentBoard.rowFlippedByColor(8, currentTeam) == endPosition.getRow()) {
            pieceType = getPromotionPiece();
            if (pieceType == null) {
                System.out.println("invalid");
                return;
            }
        }

        try {
            ws.makeMove(authToken, currentGameID, new ChessMove(startPosition, endPosition, pieceType));
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    private void resign() {
        System.out.println("resigning");
        try {
            ws.sendCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID, currentTeam);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    private void highlightLegalMoves() {
        System.out.print("starting square >> ");
        ChessPosition startPosition = getPositionInput();
        if (startPosition == null) {
            System.out.println("invalid");
            return;
        }

        try {
            ws.getValidMoves(authToken, currentGameID, startPosition);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    private void leaveGame() {
        System.out.println("leaving game");
        try {
            ws.sendCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID, currentTeam);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
        state = ClientState.LOGGED_IN;
    }

    private void helpInGame() {
        for (UIOption options : inGameOptions) {
            System.out.println(options.name() + " - " + options.description());
        }
    }

    private void displayGame(ChessGame.TeamColor team) {
        displayGame(team, null, new HashSet<>());
    }

    private void displayGame(ChessGame.TeamColor team, ChessPosition origin,
                             Collection<ChessPosition> highlightPositions) {
        if (currentBoard == null) {
            return;
        }

        int[] rows, cols;
        if (team == ChessGame.TeamColor.BLACK) {
            rows = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
            cols = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        }
        else {
            rows = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
            cols = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        }

        boolean onBorder;
        for (int row : rows) {
            onBorder = row == 0 || row == 9;

            for (int col : cols) {
                if (onBorder) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
                    System.out.print(getColumnString(col));
                }
                else if (col == 0 || col == 9 ) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
                    System.out.print(getRowString(row));
                }
                else {
                    ChessPosition position = new ChessPosition(row, col);
                    ChessPiece piece = currentBoard.getPiece(position);
                    boolean isOrigin = position.equals(origin);
                    boolean isHighlighted = highlightPositions.contains(position);

                    setBackground(position, isOrigin, isHighlighted);
                    System.out.print(getPieceString(piece));
                }
            }

            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private String getColumnString(int col) {
        return switch (col) {
            case 1 -> EscapeSequences.WIDE_A;
            case 2 -> EscapeSequences.WIDE_B;
            case 3 -> EscapeSequences.WIDE_C;
            case 4 -> EscapeSequences.WIDE_D;
            case 5 -> EscapeSequences.WIDE_E;
            case 6 -> EscapeSequences.WIDE_F;
            case 7 -> EscapeSequences.WIDE_G;
            case 8 -> EscapeSequences.WIDE_H;
            default -> EscapeSequences.EMPTY;
        };
    }

    private String getRowString(int row) {
        return switch (row) {
            case 1 -> EscapeSequences.WIDE_1;
            case 2 -> EscapeSequences.WIDE_2;
            case 3 -> EscapeSequences.WIDE_3;
            case 4 -> EscapeSequences.WIDE_4;
            case 5 -> EscapeSequences.WIDE_5;
            case 6 -> EscapeSequences.WIDE_6;
            case 7 -> EscapeSequences.WIDE_7;
            case 8 -> EscapeSequences.WIDE_8;
            default -> EscapeSequences.EMPTY;
        };
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

    private void setBackground(ChessPosition position, boolean isOrigin, boolean isHighlighted) {
        if (isOrigin) {
            System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
        }

        else if ((position.getRow() + position.getColumn())%2 == 1) {
            if (isHighlighted) {
                System.out.print(EscapeSequences.SET_BG_COLOR_GREEN);
            }
            else {
                System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            }
        }
        else {
            if (isHighlighted) {
                System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
            }
            else {
                System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
            }
        }
    }

    private ChessPosition getPositionInput() {
        String line = scanner.nextLine();
        if (line.isBlank()) {
            return null;
        }

        int col = Character.toLowerCase(line.charAt(0)) - 'a' + 1;
        int row = Character.getNumericValue(line.charAt(1));
        if (1 > col || col > 8 || 1 > row || row > 8) {
            return null;
        }

        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType getPromotionPiece() {
        System.out.println("0 - queen");
        System.out.println("1 - rook");
        System.out.println("2 - bishop");
        System.out.println("3 - knight");
        System.out.println("promotion piece >> ");

        String line = scanner.nextLine();
        if (line.isBlank()) {
            return null;
        }

        int index = Character.getNumericValue(line.charAt(0));
        return switch (index) {
            case 0 -> ChessPiece.PieceType.QUEEN;
            case 1 -> ChessPiece.PieceType.ROOK;
            case 2 -> ChessPiece.PieceType.BISHOP;
            case 3 -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }
}
