package websocket.messages;

import chess.ChessBoard;

public class LoadGameMessage extends ServerMessage {

    private final ChessBoard game;

    public LoadGameMessage(ChessBoard game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }
}
