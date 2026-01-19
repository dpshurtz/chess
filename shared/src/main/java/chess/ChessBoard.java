package chess;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

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
        resetTeam(ChessGame.TeamColor.WHITE);
        resetTeam(ChessGame.TeamColor.BLACK);
        for (int row = 3; row <= 6; row++) {
            for (int col = 1; row <= 8; row++)
                squares[row - 1][col - 1] = null;
        }
    }

    private void resetTeam(ChessGame.TeamColor pieceColor) {
        resetPiece(pieceColor, ChessPiece.PieceType.KING);
        resetPiece(pieceColor, ChessPiece.PieceType.QUEEN);
        resetPiece(pieceColor, ChessPiece.PieceType.BISHOP);
        resetPiece(pieceColor, ChessPiece.PieceType.KNIGHT);
        resetPiece(pieceColor, ChessPiece.PieceType.ROOK);
        resetPiece(pieceColor, ChessPiece.PieceType.PAWN);
    }

    private void resetPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        for (ChessPosition startPosition : getStartPositions(pieceColor, type)) {
            addPiece(startPosition, new ChessPiece(pieceColor, type));
        }
    }

    private HashSet<ChessPosition> getStartPositions(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        HashSet<ChessPosition> startPositions = new HashSet<ChessPosition>();
        switch (type) {
            case KING:
                startPositions.add(new ChessPosition(rowFlippedByColor(1, pieceColor), 5));
                break;

            case QUEEN:
                startPositions.add(new ChessPosition(rowFlippedByColor(1, pieceColor), 4));
                break;

            case BISHOP:
                startPositions.add(new ChessPosition(rowFlippedByColor(1, pieceColor), 3));
                startPositions.add(new ChessPosition(rowFlippedByColor(1, pieceColor), 6));
                break;

            case KNIGHT:
                startPositions.add(new ChessPosition(rowFlippedByColor(1, pieceColor), 2));
                startPositions.add(new ChessPosition(rowFlippedByColor(1, pieceColor), 7));
                break;

            case ROOK:
                startPositions.add(new ChessPosition(rowFlippedByColor(1, pieceColor), 1));
                startPositions.add(new ChessPosition(rowFlippedByColor(1, pieceColor), 8));
                break;

            case PAWN:
                for (int i = 1; i <= 8; i++) {
                    startPositions.add(new ChessPosition(rowFlippedByColor(2, pieceColor), i));
                }
                break;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
