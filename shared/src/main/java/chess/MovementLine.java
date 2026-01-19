package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class MovementLine {

    List<ChessPosition> positionSequence = new ArrayList<ChessPosition>();

    public MovementLine(ChessPosition origin, Direction direction){

    }
    
    public void addPosition(ChessPosition position) {
        positionSequence.add(position);
    }

    public ChessPosition get(int index) {
        return positionSequence.get(index);
    }

    private Collection<ChessPosition> filterBlockedDestinations(ChessBoard board, ChessGame.TeamColor pieceColor) {
        Collection<ChessPosition> filteredDestinations = new HashSet<ChessPosition>();
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
