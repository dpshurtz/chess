package websocket.messages;

import chess.ChessBoard;

public class LoadGameMessage extends ServerMessage {

    private final ChessBoard game;

    public LoadGameMessage(ServerMessageType type, ChessBoard game) {
        super(type);
        this.game = game;
    }
}
