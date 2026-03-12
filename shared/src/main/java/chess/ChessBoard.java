package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] squares = new ChessPiece[8][8];
    private final HashSet<ChessPosition> positions = new HashSet<>();

    public ChessBoard() {
        // Generates a set of the positions on the board
        for (int i=1; i<=squares.length; i++) {
            for (int j=1; j<=squares.length; j++) {
                positions.add(new ChessPosition(i, j));
            }
        }
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
        if (outOfBounds(position)) {
            return null;
        }
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
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (outOfBounds(move.getEndPosition()) || outOfBounds(move.getStartPosition())) {
            throw new InvalidMoveException();
        }

        // Check for special move types that change a piece not on the move squares
        moveRookIfCastled(move);
        removePawnIfEnPassant(move);

        // Find promotion piece, if any
        ChessPiece newPiece = getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            newPiece = new ChessPiece(newPiece.getTeamColor(), move.getPromotionPiece());
        }

        // Add the moving piece to the destination, and remove it from the origin
        addPiece(move.getEndPosition(), newPiece);
        addPiece(move.getStartPosition(), null);
    }

    /**
     * Checks if a move is a castle, then moves the corresponding
     * rook to its new location if it is
     *
     * @param move chess move being performed
     */
    private void moveRookIfCastled(ChessMove move) {
        ChessPiece piece = getPiece(move.getStartPosition());

        // Check if a king is moving from the starting square
        if (piece.getPieceType() == ChessPiece.PieceType.KING && move.getStartPosition().getColumn() == 5) {
            int homeRow = move.getStartPosition().getRow();
            if (move.getEndPosition().getColumn() == 3) {
                // King moved two squares queenside, so move that rook
                ChessPosition rookStart = new ChessPosition(homeRow, 1);
                addPiece(new ChessPosition(homeRow, 4), getPiece(rookStart));
                addPiece(rookStart, null);
            }
            else if (move.getEndPosition().getColumn() == 7) {
                // King moved two squares kingside, so move that rook
                ChessPosition rookStart = new ChessPosition(homeRow, 8);
                addPiece(new ChessPosition(homeRow, 6), getPiece(rookStart));
                addPiece(rookStart, null);
            }
        }
    }

    /**
     * Checks if a move is an en passant, then removes the pawn
     * that was captured if it is
     *
     * @param move chess move being performed
     */
    private void removePawnIfEnPassant(ChessMove move) {
        ChessPiece newPiece = getPiece(move.getStartPosition());
        ChessPiece oldPiece = getPiece(move.getEndPosition());

        // Check if a pawn is moving to an empty square on a new column
        if (newPiece.getPieceType() == ChessPiece.PieceType.PAWN && oldPiece == null &&
                move.getStartPosition().getColumn() != move.getEndPosition().getColumn()) {
            // Remove the pawn that must have been taken by the en passant
            addPiece(new ChessPosition(
                    rowFlippedByColor(5, newPiece.getTeamColor()),
                    move.getEndPosition().getColumn()), null
            );
        }
    }

    /**
     * Loops through every square on the board and gets the movement lines from that position
     *
     * @return HashMap mapping each location on the board to the collection of
     * movement lines available at that location. If there is no piece at a location,
     * that collection is empty in the map.
     */
    public HashMap<ChessPosition, HashSet<MovementLine>> getMovementLines() {
        HashMap<ChessPosition, HashSet<MovementLine>> movementLines = new HashMap<>();
        ChessPiece piece;

        for (ChessPosition position : positions){
            piece = getPiece(position);
            // If there is a piece, find which movement lines it can take
            if (piece != null) {
                movementLines.put(position, piece.getMovementLines(this, position));
            }
            // If there is no piece, add an empty set
            else {
                movementLines.put(position, new HashSet<>());
            }
        }

        return movementLines;
    }

    /**
     * @return A set of all positions on the board
     */
    public HashSet<ChessPosition> getPositions() {
        return positions;
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
        if (teamColor == ChessGame.TeamColor.WHITE) {
            return row;
        }
        else {
            return 9 - row;
        }
    }

    /**
     * @return Boolean indicating whether the given position is out of bounds
     */
    public boolean outOfBounds(ChessPosition position) {
        if (position.getRow() < 1) {
            return true;
        }
        if (position.getRow() > 8) {
            return true;
        }
        if (position.getColumn() < 1) {
            return true;
        }
        if (position.getColumn() > 8) {
            return true;
        }

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
