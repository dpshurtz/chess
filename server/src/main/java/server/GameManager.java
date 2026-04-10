package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    ConcurrentHashMap<Integer, GameTracker> gameTrackers = new ConcurrentHashMap<>();
    private final GameDAO gameDAO;

    public GameManager(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public void joinPerson(int gameID, Session person, String username)
            throws DataAccessException {
        gameTrackers.putIfAbsent(gameID, new GameTracker(gameDAO.getGame(gameID), gameDAO));
        gameTrackers.get(gameID).joinPerson(person, username);
    }

    public void leavePerson(int gameID, Session person, String username)
            throws DataAccessException {
        gameTrackers.get(gameID).leavePerson(person, username);
    }

    public void makeMove(int gameID, Session person, ChessMove move, String username)
            throws InvalidMoveException, DataAccessException {
        gameTrackers.get(gameID).makeMove(person, move, username);
    }

    public void resign(int gameID, String username)
            throws DataAccessException {
        gameTrackers.get(gameID).resign(username);
    }

    public ChessGame getGame(int gameID) {
        return gameTrackers.get(gameID).getGame();
    }

    public GameTracker.GameState getGameState(int gameID) {
        return gameTrackers.get(gameID).getGameState();
    }

    public String getNextPlayer(int gameID) {
        return gameTrackers.get(gameID).getNextPlayer();
    }

    public Collection<ChessMove> getValidMoves(Integer gameID, ChessPosition origin) {
        return gameTrackers.get(gameID).getValidMoves(origin);
    }

    public ChessGame.TeamColor getTeam(int gameID, String username) {
        return gameTrackers.get(gameID).getTeam(username);
    }

    public static class ConnectionManager {
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

        public void broadcast(int gameID, Session excludeSession, ServerMessage notification) throws IOException {
            String msg = new Gson().toJson(notification);
            for (Session c : connections.get(gameID).values()) {
                if (c.isOpen()) {
                    if (!c.equals(excludeSession)) {
                        c.getRemote().sendString(msg);
                    }
                }
            }
        }

        public void unicast(Session session, ServerMessage notification) throws IOException {
            String msg = new Gson().toJson(notification);
            if (session.isOpen()) {
                session.getRemote().sendString(msg);
            }
        }
    }
}
