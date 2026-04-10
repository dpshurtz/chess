package websocket.commands;

import chess.ChessPosition;

public class GetValidMovesCommand extends UserGameCommand {

    private final ChessPosition origin;

    public GetValidMovesCommand(String authToken, Integer gameID, ChessPosition origin) {
        super(CommandType.GET_VALID_MOVES, authToken, gameID);
        this.origin = origin;
    }

    public ChessPosition getOrigin() {
        return origin;
    }
}
