package chess;

import java.util.Collection;
import java.util.HashSet;

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
        HashSet<MovementLine.Direction> directions = new HashSet<>();
        HashSet<MovementLine> movementLines = new HashSet<>();
        HashSet<ChessPosition> validDestinations = new HashSet<>();
        int range = 0;

        switch (type) {
            case KING:
                range = 1;
                directions.add(MovementLine.Direction.UP);
                directions.add(MovementLine.Direction.DOWN);
                directions.add(MovementLine.Direction.LEFT);
                directions.add(MovementLine.Direction.RIGHT);
                directions.add(MovementLine.Direction.NE);
                directions.add(MovementLine.Direction.NW);
                directions.add(MovementLine.Direction.SE);
                directions.add(MovementLine.Direction.SW);
                break;

            case QUEEN:
                range = 7;
                directions.add(MovementLine.Direction.UP);
                directions.add(MovementLine.Direction.DOWN);
                directions.add(MovementLine.Direction.LEFT);
                directions.add(MovementLine.Direction.RIGHT);
                directions.add(MovementLine.Direction.NE);
                directions.add(MovementLine.Direction.NW);
                directions.add(MovementLine.Direction.SE);
                directions.add(MovementLine.Direction.SW);
                break;

            case BISHOP:
                range = 7;
                directions.add(MovementLine.Direction.NE);
                directions.add(MovementLine.Direction.NW);
                directions.add(MovementLine.Direction.SE);
                directions.add(MovementLine.Direction.SW);
                break;

            case KNIGHT:
                range = 1;
                directions.add(MovementLine.Direction.M1_KNIGHT);
                directions.add(MovementLine.Direction.M2_KNIGHT);
                directions.add(MovementLine.Direction.M3_KNIGHT);
                directions.add(MovementLine.Direction.M4_KNIGHT);
                directions.add(MovementLine.Direction.M5_KNIGHT);
                directions.add(MovementLine.Direction.M6_KNIGHT);
                directions.add(MovementLine.Direction.M7_KNIGHT);
                directions.add(MovementLine.Direction.M8_KNIGHT);
                break;

            case ROOK:
                range = 7;
                directions.add(MovementLine.Direction.UP);
                directions.add(MovementLine.Direction.DOWN);
                directions.add(MovementLine.Direction.LEFT);
                directions.add(MovementLine.Direction.RIGHT);
                break;

            case PAWN:
                if (myPosition.getRow() == board.rowFlippedByColor(2, pieceColor)) {
                    range = 2;
                }
                else range = 1;

                ChessPiece leftAttack, rightAttack;

                if (pieceColor == ChessGame.TeamColor.WHITE) {
                    directions.add(MovementLine.Direction.UP);
                    leftAttack = board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1));
                    rightAttack = board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1));
                    if (leftAttack != null && leftAttack.getTeamColor() != pieceColor) {
                        directions.add(MovementLine.Direction.NW);
                    }
                    if (rightAttack != null && rightAttack.getTeamColor() != pieceColor) {
                        directions.add(MovementLine.Direction.NE);
                    }
                }
                else {
                    directions.add(MovementLine.Direction.DOWN);
                    leftAttack = board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1));
                    rightAttack = board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1));
                    if (leftAttack != null && leftAttack.getTeamColor() != pieceColor) {
                        directions.add(MovementLine.Direction.SW);
                    }
                    if (rightAttack != null && rightAttack.getTeamColor() != pieceColor) {
                        directions.add(MovementLine.Direction.SE);
                    }
                }
                break;
        }

        for (MovementLine.Direction direction : directions) {
            movementLines.add(new MovementLine(myPosition, direction, range));
        }

        for (MovementLine movementLine : movementLines) {
            validDestinations.addAll(movementLine.filterBlockedDestinations(board, pieceColor));
        }

        if (type != PieceType.PAWN) {
            for (ChessPosition destination : validDestinations) {
                validMoves.add(new ChessMove(myPosition, destination, null));
            }
        }
        else {
            for (ChessPosition destination : validDestinations) {
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
}
