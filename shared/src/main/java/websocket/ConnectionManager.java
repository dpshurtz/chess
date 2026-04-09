package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<Session, Session>> connections =
            new ConcurrentHashMap<>();

    public void add(int gameID, Session session) {
        connections.putIfAbsent(gameID, new ConcurrentHashMap<>());
        connections.get(gameID).put(session, session);
    }

    public void remove(int gameID, Session session) {
        ConcurrentHashMap<Session, Session> gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        gameConnections.remove(session);
        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }
    }

    public void broadcast(int gameID, Session excludeSession, NotificationMessage notification) throws IOException {
        String msg = new Gson().toJson(notification);
        for (Session c : connections.get(gameID).values()) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }
}