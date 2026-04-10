package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    ConcurrentHashMap<Integer, GameTracker> gameTrackers = new ConcurrentHashMap<>();
    private final GameDAO gameDAO;

    public GameManager(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public void joinPerson(int gameID, Session person, ChessGame.TeamColor team)
            throws DataAccessException {
        gameTrackers.putIfAbsent(gameID, new GameTracker(gameDAO.getGame(gameID), gameDAO));
        gameTrackers.get(gameID).joinPerson(person, team);
    }

    public void leavePerson(int gameID, Session person, ChessGame.TeamColor team)
            throws DataAccessException {
        gameTrackers.get(gameID).leavePerson(person, team);
    }

    public void makeMove(int gameID, Session person, ChessMove move)
            throws InvalidMoveException, DataAccessException {
        gameTrackers.get(gameID).makeMove(person, move);
    }

    public void resign(int gameID, ChessGame.TeamColor team)
            throws DataAccessException {
        gameTrackers.get(gameID).resign(team);
    }

    public ChessGame getGame(int gameID) {
        return gameTrackers.get(gameID).getGame();
    }
}
