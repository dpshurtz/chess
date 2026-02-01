package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import chess.MovementLine.MoveType;
import chess.MovementLine.Direction;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new HashSet<>();
        HashSet<ChessPosition> validDestinations = new HashSet<>();

        HashSet<MovementLine> movementLines = getMovementLines(board, myPosition);

        // Filters blocked locations along each line and compiles all valid destinations
        for (MovementLine movementLine : movementLines) {
            validDestinations.addAll(movementLine.filterBlockedDestinations());
        }

        if (type != PieceType.PAWN) {
            // Creates moves based on the valid destinations
            for (ChessPosition destination : validDestinations) {
                validMoves.add(new ChessMove(myPosition, destination, null));
            }
        }

        // Pawns may be promoted and must be handled differently
        else {
            // Creates moves based on the valid destinations
            for (ChessPosition destination : validDestinations) {
                // If a pawn reaches the back rank, it must be promoted
                if (destination.getRow() != board.rowFlippedByColor(8, pieceColor)){
                    validMoves.add(new ChessMove(myPosition, destination, null));
                }
                else {
                    validMoves.add(new ChessMove(myPosition, destination, PieceType.QUEEN));
                    validMoves.add(new ChessMove(myPosition, destination, PieceType.BISHOP));
                    validMoves.add(new ChessMove(myPosition, destination, PieceType.KNIGHT));
                    validMoves.add(new ChessMove(myPosition, destination, PieceType.ROOK));
                }
            }
        }
        return validMoves;
    }

    public HashSet<MovementLine> getMovementLines(ChessBoard board, ChessPosition myPosition) {
        HashSet<MovementLine> movementLines = new HashSet<>();
        PieceVectors pieceVectors = getPieceVectors(board, myPosition);
        HashSet<Direction> directions = pieceVectors.directions();
        int range = pieceVectors.range();

        if (type != PieceType.PAWN) {
            // Generates movement lines based on directions available to the piece
            for (Direction direction : directions) {
                movementLines.add(new MovementLine(myPosition, direction, range, pieceColor, board));
            }
        }

        // Pawn capture rules are unique and must be handled differently
        else {
            // Generates movement lines based on directions available to the piece
            for (Direction direction : directions) {
                // Pawns may only attack along diagonals
                if (direction != Direction.UP && direction != Direction.DOWN) {
                    movementLines.add(new MovementLine(myPosition, direction, range, pieceColor, board, true, false));
                }
                else {
                    movementLines.add(new MovementLine(myPosition, direction, range, pieceColor, board, true, true));
                }
            }
        }

        return movementLines;
    }

    public PieceVectors getPieceVectors(ChessBoard board, ChessPosition myPosition) {
        HashSet<Direction> directions = new HashSet<>();
        int range = 0;

        switch (type) {
            case KING:
                // The king moves one square in any of the 8 standard directions
                range = 1;
                addDirectionsByType(directions, MoveType.ORTHOGONAL);
                addDirectionsByType(directions, MoveType.DIAGONAL);
                break;

            case QUEEN:
                // The queen moves up to 7 squares in any of the 8 standard directions
                range = 7;
                addDirectionsByType(directions, MoveType.ORTHOGONAL);
                addDirectionsByType(directions, MoveType.DIAGONAL);
                break;

            case BISHOP:
                // The bishop moves up to 7 squares along any diagonal
                range = 7;
                addDirectionsByType(directions, MoveType.DIAGONAL);
                break;

            case KNIGHT:
                // The knight moves a single time in any of its 8 unique L-shaped directions
                range = 1;
                addDirectionsByType(directions, MoveType.KNIGHT);
                break;

            case ROOK:
                // The rook moves up to 7 squares vertically or horizontally
                range = 7;
                addDirectionsByType(directions, MoveType.ORTHOGONAL);
                break;

            case PAWN:
                // If the pawn is on its starting square, it may move 2 squares
                // Otherwise, it may move only one square
                if (myPosition.getRow() == board.rowFlippedByColor(2, pieceColor)) {
                    range = 2;
                }
                else {
                    range = 1;
                }

                if (pieceColor == ChessGame.TeamColor.WHITE) {
                    addDirectionsByType(directions, MoveType.W_PAWN);
                }
                else {
                    addDirectionsByType(directions, MoveType.B_PAWN);
                }
                break;
        }

        return new PieceVectors(directions, range);
    }

    /**
     * Adds specific directions to a set given a certain type of movement
     *
     * @param directions Set to which the movement directions will be added
     * @param moveType The type of movement options to add
     */
    private void addDirectionsByType(HashSet<Direction> directions, MoveType moveType) {
        switch (moveType) {
            case ORTHOGONAL:
                directions.add(Direction.UP);
                directions.add(Direction.DOWN);
                directions.add(Direction.LEFT);
                directions.add(Direction.RIGHT);
                break;

            case DIAGONAL:
                directions.add(Direction.NE);
                directions.add(Direction.NW);
                directions.add(Direction.SE);
                directions.add(Direction.SW);
                break;

            case KNIGHT:
                directions.add(Direction.M1_KNIGHT);
                directions.add(Direction.M2_KNIGHT);
                directions.add(Direction.M3_KNIGHT);
                directions.add(Direction.M4_KNIGHT);
                directions.add(Direction.M5_KNIGHT);
                directions.add(Direction.M6_KNIGHT);
                directions.add(Direction.M7_KNIGHT);
                directions.add(Direction.M8_KNIGHT);
                break;

            case W_PAWN:
                directions.add(Direction.UP);
                directions.add(Direction.NE);
                directions.add(Direction.NW);
                break;

            case B_PAWN:
                directions.add(Direction.DOWN);
                directions.add(Direction.SE);
                directions.add(Direction.SW);
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }
}
