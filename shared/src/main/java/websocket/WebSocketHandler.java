package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand action = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (action.getCommandType()) {
                case CONNECT -> connect(action.getGameID(), action.getAuthToken(), ctx.session);
                case LEAVE -> leave(action.getGameID(), action.getAuthToken(), ctx.session);
                case RESIGN -> resign(action.getGameID(), action.getAuthToken(), ctx.session);
                case MAKE_MOVE -> make_move(((MakeMoveCommand)action).getMove(),
                        action.getGameID(), action.getAuthToken(), ctx.session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(int gameID, String authToken, Session session) throws IOException {
        connections.add(gameID, session);
        var message = String.format("%s joined the game", authToken);
        var notification = new NotificationMessage(message);
        connections.broadcast(gameID, session, notification);
    }

    private void leave(int gameID, String authToken, Session session) throws IOException {
        var message = String.format("%s left the game", authToken);
        var notification = new NotificationMessage(message);
        connections.broadcast(gameID, session, notification);
        connections.remove(gameID, session);
    }

    private void resign(Integer gameID, String authToken, Session session) {
    }

    private void make_move(ChessMove move, Integer gameID, String authToken, Session session) {
    }

}