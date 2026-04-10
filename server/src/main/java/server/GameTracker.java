package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class GameTracker {

    private final GameData gameData;
    private final GameDAO gameDAO;
    private Session whitePlayer = null;
    private Session blackPlayer = null;

    public GameTracker(GameData gameData, GameDAO gameDAO) {
        this.gameData = gameData;
        this.gameDAO = gameDAO;
    }

    public void joinPerson(Session person, ChessGame.TeamColor team) {
        if (team == ChessGame.TeamColor.WHITE) {
            whitePlayer = person;
        }
        else if (team == ChessGame.TeamColor.BLACK) {
            blackPlayer = person;
        }
    }

    public void leavePerson(Session person, ChessGame.TeamColor team) throws DataAccessException {
        if (team == ChessGame.TeamColor.WHITE) {
            whitePlayer = null;
            gameDAO.updateGame(new GameData(
                    gameData.gameID(),
                    null,
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()));
        }
        else if (team == ChessGame.TeamColor.BLACK) {
            blackPlayer = null;
            gameDAO.updateGame(new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    null,
                    gameData.gameName(),
                    gameData.game()));
        }
    }

    public void makeMove(Session person, ChessMove move)
            throws InvalidMoveException, DataAccessException {
        if (gameData.game().getTeamTurn() != getTeam(person)) {
            throw new InvalidMoveException();
        }

        gameData.game().makeMove(move);
        gameDAO.updateGame(new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                gameData.game()));
    }

    public void resign(ChessGame.TeamColor team) throws DataAccessException {
        gameData.game().resign(team);
        gameDAO.updateGame(new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                gameData.game()));
    }

    public ChessGame getGame() {
        return gameData.game();
    }

    private ChessGame.TeamColor getTeam(Session person) {
        if (person.equals(blackPlayer)) {
            return ChessGame.TeamColor.BLACK;
        }
        if (person.equals(whitePlayer)) {
            return ChessGame.TeamColor.WHITE;
        }
        return null;
    }
}
