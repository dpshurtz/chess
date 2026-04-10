package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class StandardGameCommand extends UserGameCommand {
    private final ChessGame.TeamColor team;

    public StandardGameCommand(CommandType commandType, String authToken, Integer gameID, ChessGame.TeamColor team) {
        super(commandType, authToken, gameID);
        this.team = team;
    }

    public ChessGame.TeamColor getTeam() {
        return team;
    }
}
