package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board = new ChessBoard();
    private TeamColor teamTurn = TeamColor.WHITE;
    private Collection<MovementLine> movementLines = new HashSet<>();
    private HashMap<ChessPosition, Collection<MovementLine>> underAttackByWhite = new HashMap<>();
    private HashMap<ChessPosition, Collection<MovementLine>> underAttackByBlack = new HashMap<>();
    private ChessPosition kingPosWhite;
    private ChessPosition kingPosBlack;
    private HashMap<ChessPosition, Collection<ChessMove>> validMovesFrom = new HashMap<>();

    public ChessGame() {
        board.resetBoard();
        movementLines = board.getMovementLines();
        resetAllAttacks();
        setKingPositions();
        findAllValidMoves();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        return validMovesFrom.get(startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return !isSafe(kingPos(teamColor), teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return (isInCheck(teamColor) && !hasValidMoves(teamColor));
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return (!isInCheck(teamColor) && !hasValidMoves(teamColor));
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        movementLines = board.getMovementLines();
        resetAllAttacks();
        setKingPositions();
        findAllValidMoves();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private void resetAllAttacks() {
        for (ChessPosition position : board.getPositions()) {
            underAttackByWhite.put(position, new HashSet<>());
            underAttackByBlack.put(position, new HashSet<>());
        }

        for (MovementLine movementLine : movementLines) {
            movementLine.findAttackedPositions();

            if (movementLine.getTeam() == TeamColor.WHITE) {
                for (ChessPosition position : movementLine.getAttackedPositions()) {
                    underAttackByWhite.get(position).add(movementLine);
                }
            }
            else {
                for (ChessPosition position : movementLine.getAttackedPositions()) {
                    underAttackByBlack.get(position).add(movementLine);
                }
            }
        }
    }

    private boolean isSafe(ChessPosition position, TeamColor myTeam) {
        return underAttackByTeam(enemyTeam(myTeam)).get(position).isEmpty();
    }

    private HashMap<ChessPosition, Collection<MovementLine>> underAttackByTeam(TeamColor team) {
        if (team == TeamColor.WHITE) {
            return underAttackByWhite;
        }
        else {
            return underAttackByBlack;
        }
    }

    private TeamColor enemyTeam(TeamColor team) {
        if (team == TeamColor.WHITE) {
            return TeamColor.BLACK;
        }
        else {
            return TeamColor.WHITE;
        }
    }

    private void setKingPositions() {
        ChessPiece piece;
        for (ChessPosition position : board.getPositions()) {
            piece = board.getPiece(position);
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING) {
                if (piece.getTeamColor() == TeamColor.WHITE) {
                    kingPosWhite = position;
                }
                else {
                    kingPosBlack = position;
                }
            }
        }
    }

    private ChessPosition kingPos(TeamColor team) {
        if (team == TeamColor.WHITE) {
            return kingPosWhite;
        }
        else {
            return kingPosBlack;
        }
    }

    private boolean hasValidMoves(TeamColor team) {
        ChessPiece piece;
        for (ChessPosition position : validMovesFrom.keySet()) {
            piece = board.getPiece(position);
            if (piece != null && piece.getTeamColor() == team && !validMovesFrom.get(position).isEmpty()) {
                System.out.println("Valid moves found: ");
                for (ChessMove move : validMovesFrom.get(position)) {
                    System.out.println(move);
                }
                return true;
            }
        }
        return false;
    }

    private void findAllValidMoves() {
        for (ChessPosition position : board.getPositions()) {
            validMovesFrom.put(position, validMovesPrecalculate(position));
        }
    }

    private Collection<ChessMove> validMovesPrecalculate(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        TeamColor team = piece.getTeamColor();

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            return piece.pieceMoves(board, startPosition).stream()
                    .filter(move -> isSafe(move.getEndPosition(), team))
                    .collect(Collectors.toSet());
        }

        else {
            Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
            System.out.println("Original moves");
            System.out.println(moves);

            if (isInCheck(team)) {
                for (MovementLine movementLine : underAttackByTeam(enemyTeam(team)).get(kingPos(team))) {
                    moves.removeIf(move ->
                            !movementLine.getAttackedPositions().contains(move.getEndPosition()) &&
                                    !movementLine.getPositionSequence().getFirst().equals(move.getEndPosition())
                    );
                }
            }

            for (MovementLine movementLine : underAttackByTeam(enemyTeam(team)).get(startPosition)) {
                if (movementLine.isPinned(startPosition)) {
                    moves.removeIf(move -> !movementLine.getPositionSequence().contains(move.getEndPosition()));
                }
            }

            System.out.println("Filtered moves");
            System.out.println(moves);
            return moves;
        }
    }
}
