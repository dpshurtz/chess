package chess;

import java.util.HashSet;
import java.util.ArrayList;

public class MovementLine {

    ArrayList<ChessPosition> positionSequence = new ArrayList<>();
    boolean noAttack = false;

    public MovementLine(ChessPosition origin, Direction direction, int range) {
        this(origin, direction, range, false);
    }

    public MovementLine(ChessPosition origin, Direction direction, int range, boolean noAttack) {
        this.noAttack = noAttack;

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

        int[] location = new int[]{ origin.getRow(), origin.getColumn() };
        for (int i = 0; i < range; i++) {
            location[0] += unitVector[0];
            location[1] += unitVector[1];
            positionSequence.add(new ChessPosition(location[0], location[1]));
        }
    }

    public HashSet<ChessPosition> filterBlockedDestinations(ChessBoard board, ChessGame.TeamColor pieceColor) {
        HashSet<ChessPosition> filteredDestinations = new HashSet<>();
        boolean blocked = false;
        for (ChessPosition destination : positionSequence) {
            if (blocked || board.outOfBounds(destination)) break;
            ChessPiece targetPiece = board.getPiece(destination);

            if (targetPiece == null) {
                filteredDestinations.add(destination);
            }
            else if (targetPiece.getTeamColor() == pieceColor) {
                break;
            }
            else {
                if (noAttack) break;
                filteredDestinations.add(destination);
                blocked = true;
            }
        }
        return filteredDestinations;
    }

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
}
