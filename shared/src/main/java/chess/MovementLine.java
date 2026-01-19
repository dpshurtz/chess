package chess;

import java.util.HashSet;
import java.util.ArrayList;

/**
 * Represents a line along which a chess piece can move
 * Since another piece may block all movement past a certain point in a line,
 * the positions in the line are organized in a sequence.
 */
public class MovementLine {

    ArrayList<ChessPosition> positionSequence = new ArrayList<>();
    boolean noAttack = false;

    // Constructor for lines along which an attack may be valid
    public MovementLine(ChessPosition origin, Direction direction, int range) {
        this(origin, direction, range, false);
    }

    // Constructor that allows the validity of attack along a line to be disabled
    public MovementLine(ChessPosition origin, Direction direction, int range, boolean noAttack) {
        this.noAttack = noAttack;

        // Generates a vector representing a single step for each movement direction
        int[] unitVector;
        switch (direction) {
            case UP ->          unitVector = new int[]{ 1,  0};
            case DOWN ->        unitVector = new int[]{-1,  0};
            case LEFT ->        unitVector = new int[]{ 0, -1};
            case RIGHT ->       unitVector = new int[]{ 0,  1};
            case NE ->          unitVector = new int[]{ 1,  1};
            case NW ->          unitVector = new int[]{ 1, -1};
            case SE ->          unitVector = new int[]{-1,  1};
            case SW ->          unitVector = new int[]{-1, -1};
            case M1_KNIGHT ->   unitVector = new int[]{ 1,  2};
            case M2_KNIGHT ->   unitVector = new int[]{ 2,  1};
            case M3_KNIGHT ->   unitVector = new int[]{ 2, -1};
            case M4_KNIGHT ->   unitVector = new int[]{ 1, -2};
            case M5_KNIGHT ->   unitVector = new int[]{-1, -2};
            case M6_KNIGHT ->   unitVector = new int[]{-2, -1};
            case M7_KNIGHT ->   unitVector = new int[]{-2,  1};
            case M8_KNIGHT ->   unitVector = new int[]{-1,  2};
            default ->          unitVector = new int[]{ 0,  0};
        }

        // Moves forward a unit in the given direction, repeating based on the range
        // Adds each step to the sequence of positions in the line
        int[] location = new int[]{ origin.getRow(), origin.getColumn() };
        for (int i = 0; i < range; i++) {
            location[0] += unitVector[0];
            location[1] += unitVector[1];
            positionSequence.add(new ChessPosition(location[0], location[1]));
        }
    }

    /**
     * Filters the line of movement, not including any positions in the sequence that
     * are beyond a blocking piece or the edge of the board
     *
     * @param board The current configuration of pieces on the board
     * @param pieceColor Color of the piece attempting to move
     * @return HashSet of all locations in the line that are not blocked by another piece or the edge of the board
     */
    public HashSet<ChessPosition> filterBlockedDestinations(ChessBoard board, ChessGame.TeamColor pieceColor) {
        HashSet<ChessPosition> filteredDestinations = new HashSet<>();
        boolean blocked = false;

        for (ChessPosition destination : positionSequence) {
            // If the line is blocked or out of bounds, do not include the rest of the line
            if (blocked || board.outOfBounds(destination)) {
                break;
            }

            ChessPiece targetPiece = board.getPiece(destination);
            if (targetPiece == null) {
                // The target square is empty, so movement is unhindered
                filteredDestinations.add(destination);
            }
            else if (targetPiece.getTeamColor() == pieceColor) {
                // The target square contains a friendly piece, so further movement is impossible
                break;
            }
            else {
                // The target square contains an enemy piece, so further movement is impossible
                // However, if attacks are allowed along this line, capturing the enemy piece is valid
                if (noAttack) {
                    break;
                }
                filteredDestinations.add(destination);
                blocked = true;
            }
        }
        return filteredDestinations;
    }

    /**
     * The various movement directions possible for chess pieces
     */
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        NE,
        NW,
        SE,
        SW,
        M1_KNIGHT,
        M2_KNIGHT,
        M3_KNIGHT,
        M4_KNIGHT,
        M5_KNIGHT,
        M6_KNIGHT,
        M7_KNIGHT,
        M8_KNIGHT
    }

    /**
     * The various types of movement possible for chess pieces
     */
    public enum MoveType {
        ORTHOGONAL,
        DIAGONAL,
        KNIGHT,
        PAWN
    }
}
