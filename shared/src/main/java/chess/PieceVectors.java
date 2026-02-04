package chess;

import java.util.HashSet;

/**
 * Holds all directions a piece can move, along with its range
 */
public record PieceVectors(HashSet<MovementLine.Direction> directions, int range) {
}
