package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getColumn() - 1][position.getRow() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (outOfBounds(position)) return null;
        return squares[position.getColumn() - 1][position.getRow() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        resetBoard(ChessGame.TeamColor.WHITE);
        resetBoard(ChessGame.TeamColor.BLACK);
        for (int row = 3; row <= 6; row++) {
            for (int col = 1; row <= 8; row++)
                squares[row - 1][col - 1] = null;
        }
    }

    private void resetBoard(ChessGame.TeamColor pieceColor) {
        resetBoard(pieceColor, ChessPiece.PieceType.KING);
        resetBoard(pieceColor, ChessPiece.PieceType.QUEEN);
        resetBoard(pieceColor, ChessPiece.PieceType.BISHOP);
        resetBoard(pieceColor, ChessPiece.PieceType.KNIGHT);
        resetBoard(pieceColor, ChessPiece.PieceType.ROOK);
        resetBoard(pieceColor, ChessPiece.PieceType.PAWN);
    }

    private void resetBoard(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        for (ChessPosition startPosition : getStartPositions(pieceColor, type)) {
            addPiece(startPosition, new ChessPiece(pieceColor, type));
        }
    }

    private ChessPosition[] getStartPositions(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        ChessPosition[] startPositions;
        switch (type) {
            case KING:
                startPositions = new ChessPosition[]{ new ChessPosition(rowFlippedByColor(1, pieceColor), 5) };
                break;

            case QUEEN:
                startPositions = new ChessPosition[]{ new ChessPosition(rowFlippedByColor(1, pieceColor), 4) };
                break;

            case BISHOP:
                startPositions = new ChessPosition[2];
                startPositions[0] = new ChessPosition(rowFlippedByColor(1, pieceColor), 3);
                startPositions[1] = new ChessPosition(rowFlippedByColor(1, pieceColor), 6);
                break;

            case KNIGHT:
                startPositions = new ChessPosition[2];
                startPositions[0] = new ChessPosition(rowFlippedByColor(1, pieceColor), 2);
                startPositions[1] = new ChessPosition(rowFlippedByColor(1, pieceColor), 7);
                break;

            case ROOK:
                startPositions = new ChessPosition[2];
                startPositions[0] = new ChessPosition(rowFlippedByColor(1, pieceColor), 1);
                startPositions[1] = new ChessPosition(rowFlippedByColor(1, pieceColor), 8);
                break;

            case PAWN:
                startPositions = new ChessPosition[8];
                for (int i=0; i<8; i++) {
                    startPositions[i] = new ChessPosition(rowFlippedByColor(2, pieceColor), i + 1);
                }
                break;

            default:
                startPositions = new ChessPosition[]{ null };
        }

        return startPositions;
    }

    public int rowFlippedByColor(int row, ChessGame.TeamColor pieceColor) {
        if (pieceColor == ChessGame.TeamColor.WHITE) return row;
        else return 9 - row;
    }

    public boolean outOfBounds(ChessPosition position) {
        if (position.getRow() < 1) return true;
        if (position.getRow() > 8) return true;
        if (position.getColumn() < 1) return true;
        if (position.getColumn() > 8) return true;

        return false;
    }
}
