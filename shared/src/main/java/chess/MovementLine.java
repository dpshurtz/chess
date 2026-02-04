package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Represents a line along which a chess piece can move
 * Since another piece may block all movement past a certain point in a line,
 * the positions in the line are organized in a sequence.
 */
public class MovementLine {

    private final ArrayList<ChessPosition> positionSequence = new ArrayList<>();
    private final boolean noAttack;
    private final boolean isPawn;
    private final ChessGame.TeamColor team;
    private final ChessBoard board;
    private Collection<ChessPosition> attackedPositions;

    // Constructor for lines along which an attack may be valid
    public MovementLine(ChessPosition origin, Direction direction, int range, 
                        ChessGame.TeamColor team, ChessBoard board) {
        this(origin, direction, range, team, board, false, false);
    }

    // Constructor that allows the validity of attack along a line to be disabled
    public MovementLine(ChessPosition origin, Direction direction, int range, 
                        ChessGame.TeamColor team, ChessBoard board, boolean isPawn, boolean noAttack) {
        this.isPawn = isPawn;
        this.noAttack = noAttack;
        this.team = team;
        this.board = board;

        // Generates a vector representing a single step for each movement direction
        int[] unitVector = getUnitVector(direction);

        // Moves forward a unit in the given direction, repeating based on the range
        // Adds each step to the sequence of positions in the line
        int[] location = new int[]{ origin.getRow(), origin.getColumn() };
        positionSequence.add(origin);
        for (int i = 0; i < range; i++) {
            location[0] += unitVector[0];
            location[1] += unitVector[1];
            positionSequence.add(new ChessPosition(location[0], location[1]));
        }

        // Initializes the attackedPositions set
        findAttackedPositions();
    }

    /**
     * Converts a chess direction into a 2d vector, represented by an int[2]
     */
    private static int[] getUnitVector(Direction direction) {
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
        return unitVector;
    }

    /**
     * @return Which team this movement line corresponds to
     */
    public ChessGame.TeamColor getTeam() {
        return team;
    }

    /**
     * @return A set of all positions attacked by this movement line
     */
    public Collection<ChessPosition> getAttackedPositions() {
        return attackedPositions;
    }

    /**
     * @return The sequence of positions along this line, from its origin out to its range
     */
    public ArrayList<ChessPosition> getPositionSequence() {
        return positionSequence;
    }

    /**
     * Filters the line of movement, not including any positions in the sequence that
     * are beyond a blocking piece or the edge of the board
     *
     * @return HashSet of all locations in the line that are not blocked by another piece or the edge of the board
     */
    public HashSet<ChessPosition> filterBlockedDestinations() {
        HashSet<ChessPosition> filteredDestinations = new HashSet<>();

        // Loop through all positions except the line's origin
        for (ChessPosition destination : positionSequence.subList(1, positionSequence.size())) {
            // If the line is blocked or out of bounds, do not include the rest of the line
            if (board.outOfBounds(destination)) {
                break;
            }

            ChessPiece targetPiece = board.getPiece(destination);
            if (targetPiece == null) {
                // The pawn cannot move diagonally if no piece is there
                if (isPawn && !noAttack) {
                    break;
                }
                // The target square is empty, so movement is unhindered
                filteredDestinations.add(destination);
            }
            else if (targetPiece.getTeamColor() == team) {
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
                break;
            }
        }
        return filteredDestinations;
    }

    /**
     * Updates the attackedPositions set
     * Attacked squares are distinct from the filtered squares
     * because a piece may threaten a location it cannot currently move to
     */
    public void findAttackedPositions() {
        HashSet<ChessPosition> filteredDestinations = new HashSet<>();

        // Loop through all positions except the line's origin
        for (ChessPosition destination : positionSequence.subList(1, positionSequence.size())) {
            // If the line is blocked or out of bounds, do not include the rest of the line
            if (board.outOfBounds(destination)) {
                break;
            }

            ChessPiece targetPiece = board.getPiece(destination);
            if (targetPiece == null) {
                // The target square is empty, so movement is unhindered
                filteredDestinations.add(destination);
                // The pawn can only threaten an adjacent diagonal square
                if (isPawn && !noAttack) {
                    break;
                }
            }
            else {
                // The target square contains another piece, so further movement is impossible
                // However, if attacks are allowed along this line, the square is still "attacked"
                if (noAttack) {
                    break;
                }
                filteredDestinations.add(destination);
                break;
            }
        }
        attackedPositions = filteredDestinations;
    }

    /**
     * Determines if a piece is the only thing keeping the king safe from
     * an attack along this movement line
     *
     * @return A boolean indicating whether the piece at the specified position
     * is pinned to the movement line
     */
    public boolean isPinned(ChessPosition position) {
        boolean pastBlockingPiece = false;
        ChessPiece target;

        for (ChessPosition destination : positionSequence) {
            // Skip through the movement line until the index past the given location
            if (!pastBlockingPiece) {
                if (destination.equals(position)) {
                    pastBlockingPiece = true;
                }
                continue;
            }

            // If another piece is encountered, there is no pin
            // If the king is found first, there is a pin
            target = board.getPiece(destination);
            if (target != null) {
                return (target.getPieceType() == ChessPiece.PieceType.KING && target.getTeamColor() != team);
            }
        }
        return false;
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
        W_PAWN,
        B_PAWN
    }

    @Override
    public String toString() {
        return positionSequence.toString();
    }
}
