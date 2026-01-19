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
        squares = new ChessPiece[8][8];
        resetTeam(ChessGame.TeamColor.WHITE);
        resetTeam(ChessGame.TeamColor.BLACK);
    }

    /**
     * Sets one team to the default starting configuration
     */
    private void resetTeam(ChessGame.TeamColor pieceColor) {
        resetPiece(pieceColor, ChessPiece.PieceType.KING);
        resetPiece(pieceColor, ChessPiece.PieceType.QUEEN);
        resetPiece(pieceColor, ChessPiece.PieceType.BISHOP);
        resetPiece(pieceColor, ChessPiece.PieceType.KNIGHT);
        resetPiece(pieceColor, ChessPiece.PieceType.ROOK);
        resetPiece(pieceColor, ChessPiece.PieceType.PAWN);
    }

    /**
     * Sets a piece type on a team in its default starting positions
     */
    private void resetPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        for (ChessPosition startPosition : getStartPositions(pieceColor, type)) {
            addPiece(startPosition, new ChessPiece(pieceColor, type));
        }
    }

    /**
     * Determines what the default starting positions are for a given piece
     *
     * @return HashSet of starting positions
     */
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

    /**
     * Counts rows from the top of the board if the team is black
     * If team is white, counts from the bottom
     *
     * @param row The row number relative to the given team
     * @param teamColor Color of the team relative to which the row was given
     * @return The row number relative to board
     */
    public int rowFlippedByColor(int row, ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) return row;
        else return 9 - row;
    }

    /**
     * @return Boolean indicating whether the given position is out of bounds
     */
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

    @Override
    public String toString() {
        return "ChessBoard{" +
                "squares=" + Arrays.deepToString(squares) +
                '}';
    }
}
