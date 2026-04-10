package handler;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
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
import websocket.ConnectionManager;
import websocket.commands.*;
import websocket.messages.*;


import dataaccess.AuthDAO;
import dataaccess.GameDAO;

import java.io.IOException;
import java.util.Collection;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
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
            gameManager.joinPerson(action.getGameID(), session, action.getTeam());
            var message = String.format(
                    "%s joined the game as %s",
                    authDAO.getAuth(action.getAuthToken()).username(),
                    teamToString(action.getTeam())
            );

            var notification = new NotificationMessage(message);
            connections.broadcast(action.getGameID(), session, notification);

            LoadGameMessage loadGameMessage =
                    new LoadGameMessage(gameManager.getGame(action.getGameID()).getBoard());
            connections.unicast(session, loadGameMessage);
        }
        catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void leave(StandardGameCommand action, Session session) throws IOException {
        try {
            var message = String.format(
                    "%s left the game",
                    authDAO.getAuth(action.getAuthToken()).username()
            );
            var notification = new NotificationMessage(message);
            connections.broadcast(action.getGameID(), session, notification);
            connections.remove(action.getGameID(), session);
            gameManager.leavePerson(action.getGameID(), session, action.getTeam());
        }
        catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void resign(StandardGameCommand action, Session session) throws IOException {
        try {
            gameManager.resign(action.getGameID(), action.getTeam());
            var message = String.format("%s resigned from the game",
                    authDAO.getAuth(action.getAuthToken()).username()
            );
            var notification = new NotificationMessage(message);
            connections.broadcast(action.getGameID(), session, notification);
        }
        catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeMove(MakeMoveCommand action, Session session) throws IOException {
        try {
            gameManager.makeMove(action.getGameID(), session, action.getMove());

            LoadGameMessage loadGameMessage =
                    new LoadGameMessage(gameManager.getGame(action.getGameID()).getBoard());
            connections.broadcast(action.getGameID(), null, loadGameMessage);

            var message = String.format("%s made the move: %s",
                    authDAO.getAuth(action.getAuthToken()).username(),
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
        catch (DataAccessException e) {
            throw new RuntimeException(e);
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