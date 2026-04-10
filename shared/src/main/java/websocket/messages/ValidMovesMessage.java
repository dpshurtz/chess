package websocket.messages;

import chess.ChessMove;

import java.util.Collection;

public class ValidMovesMessage extends ServerMessage {

    private final Collection<ChessMove> validMoves;

    public ValidMovesMessage(Collection<ChessMove> validMoves) {
        super(ServerMessageType.VALID_MOVES);
        this.validMoves = validMoves;
    }

    public Collection<ChessMove> getValidMoves() {
        return validMoves;
    }
}
