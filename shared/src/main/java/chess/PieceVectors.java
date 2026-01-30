package chess;

import java.util.HashSet;

public record PieceVectors(HashSet<MovementLine.Direction> directions, int range) {
}
