package handler;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import server.GameManager;
import server.GameTracker;
import websocket.commands.*;
import websocket.messages.*;


import dataaccess.AuthDAO;
import dataaccess.GameDAO;

import java.io.IOException;
import java.util.Collection;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final GameManager.ConnectionManager connections = new GameManager.ConnectionManager();
    private final GameManager gameManager;
    private final AuthDAO authDAO;

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        gameManager = new GameManager(gameDAO);
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand action = new GsonBuilder()
                    .registerTypeAdapter(UserGameCommand.class, new CommandDeserializer())
                    .create()
                    .fromJson(ctx.message(), UserGameCommand.class);
            switch (action.getCommandType()) {
                case CONNECT -> connect((StandardGameCommand) action, ctx.session);
                case LEAVE -> leave((StandardGameCommand) action, ctx.session);
                case RESIGN -> resign((StandardGameCommand) action, ctx.session);
                case MAKE_MOVE -> makeMove((MakeMoveCommand)action, ctx.session);
                case GET_VALID_MOVES -> getValidMoves((GetValidMovesCommand)action, ctx.session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(StandardGameCommand action, Session session) throws IOException {
        connections.add(action.getGameID(), session);
        try {
            String username = authDAO.getAuth(action.getAuthToken()).username();
            gameManager.joinPerson(action.getGameID(), session, username);
            var message = String.format(
                    "%s joined the game as %s",
                    username,
                    teamToString(gameManager.getTeam(action.getGameID(), username))
            );

            var notification = new NotificationMessage(message);
            connections.broadcast(action.getGameID(), session, notification);

            LoadGameMessage loadGameMessage =
                    new LoadGameMessage(gameManager.getGame(action.getGameID()).getBoard());
            connections.unicast(session, loadGameMessage);
        }
        catch (DataAccessException | NullPointerException e) {
            connections.unicast(session, new ErrorMessage("unauthorized"));
        }
    }

    private void leave(StandardGameCommand action, Session session) throws IOException {
        try {
            String username = authDAO.getAuth(action.getAuthToken()).username();
            var message = String.format(
                    "%s left the game",
                    username
            );
            var notification = new NotificationMessage(message);
            connections.broadcast(action.getGameID(), session, notification);
            connections.remove(action.getGameID(), session);
            gameManager.leavePerson(action.getGameID(), session, username);
        }
        catch (DataAccessException | NullPointerException e) {
            connections.unicast(session, new ErrorMessage("unauthorized"));
        }
    }

    private void resign(StandardGameCommand action, Session session) throws IOException {
        try {
            String username = authDAO.getAuth(action.getAuthToken()).username();
            gameManager.resign(action.getGameID(), username);
            var message = String.format("%s resigned from the game",
                    username
            );
            var notification = new NotificationMessage(message);
            connections.broadcast(action.getGameID(), null, notification);
        }
        catch (DataAccessException | NullPointerException e) {
            connections.unicast(session, new ErrorMessage("unauthorized"));
        }
    }

    private void makeMove(MakeMoveCommand action, Session session) throws IOException {
        try {
            String username = authDAO.getAuth(action.getAuthToken()).username();
            gameManager.makeMove(action.getGameID(), session, action.getMove(), username);

            LoadGameMessage loadGameMessage =
                    new LoadGameMessage(gameManager.getGame(action.getGameID()).getBoard());
            connections.broadcast(action.getGameID(), null, loadGameMessage);

            var message = String.format("%s made the move: %s",
                    username,
                    action.getMove().toString()
            );
            var notification = new NotificationMessage(message);
            connections.broadcast(action.getGameID(), session, notification);

            GameTracker.GameState gameState = gameManager.getGameState(action.getGameID());
            if (gameState == GameTracker.GameState.NONE) {
                return;
            }

            message = String.format(
                    "%s is in ",
                    gameManager.getNextPlayer(action.getGameID())
            );
            switch (gameState) {
                case CHECK -> message += "check!";
                case CHECKMATE -> message += "checkmate!";
                case STALEMATE -> message += "stalemate!";
            }
            notification = new NotificationMessage(message);
            connections.broadcast(action.getGameID(), null, notification);
        }
        catch (DataAccessException | NullPointerException e) {
            connections.unicast(session, new ErrorMessage("unauthorized"));
        }
        catch (InvalidMoveException e) {
            connections.unicast(session, new ErrorMessage("invalid move"));
        }
    }

    private void getValidMoves(GetValidMovesCommand action, Session session) throws IOException {
        Collection<ChessMove> validMoves =
                gameManager.getValidMoves(action.getGameID(), action.getOrigin());
        connections.unicast(session, new ValidMovesMessage(validMoves));
    }

    private String teamToString(ChessGame.TeamColor team) {
        if (team == null) {
            return "an observer";
        }
        return switch (team) {
            case WHITE -> "white";
            case BLACK -> "black";
        };
    }
}