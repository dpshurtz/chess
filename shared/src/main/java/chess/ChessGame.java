package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Objects;
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
    private HashMap<ChessPosition, Collection<MovementLine>> movementLinesByOrigin;
    private HashMap<ChessPosition, Collection<MovementLine>> underAttackByWhite = new HashMap<>();
    private HashMap<ChessPosition, Collection<MovementLine>> underAttackByBlack = new HashMap<>();
    private ChessPosition kingPosWhite;
    private ChessPosition kingPosBlack;
    private HashMap<ChessPosition, Collection<ChessMove>> validMovesFrom = new HashMap<>();

    public ChessGame() {
        board.resetBoard();
        movementLinesByOrigin = board.getMovementLines();
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
        if (board.getPiece(startPosition) == null) {
            return null;
        }
        return validMovesFrom.get(startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }
        if (!validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException();
        }

        board.makeMove(move);

        ChessPosition origin = move.getStartPosition();
        ChessPosition destination = move.getEndPosition();
        Collection<MovementLine> oldMovementLines;

        System.out.println("\nTurn");

        // piece's lines updates
        System.out.println("Old lines from origin");
        for (MovementLine movementLine : movementLinesByOrigin.get(origin)) {
            System.out.println(movementLine);
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByTeam(teamTurn).get(position).remove(movementLine);
            }
        }
        movementLinesByOrigin.get(origin).clear();
        System.out.println("Old lines from destination");
        for (MovementLine movementLine : movementLinesByOrigin.get(destination)) {
            System.out.println(movementLine);
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByTeam(enemyTeam(teamTurn)).get(position).remove(movementLine);
            }
        }
        movementLinesByOrigin.put(destination, piece.getMovementLines(board, destination));
        System.out.println("New lines from destination");
        for (MovementLine movementLine : movementLinesByOrigin.get(destination)) {
            System.out.println(movementLine);
            movementLine.findAttackedPositions();
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByTeam(teamTurn).get(position).add(movementLine);
            }
        }

        // destination updates
        System.out.println("White lines passing through destination");
        oldMovementLines = new HashSet<>(underAttackByTeam(TeamColor.WHITE).get(destination));
        for (MovementLine movementLine : oldMovementLines) {
            System.out.println(movementLine);
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByWhite.get(position).remove(movementLine);
            }
            movementLine.findAttackedPositions();
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByWhite.get(position).add(movementLine);
            }
        }
        System.out.println("Black lines passing through destination");
        oldMovementLines = new HashSet<>(underAttackByTeam(TeamColor.BLACK).get(destination));
        for (MovementLine movementLine : oldMovementLines) {
            System.out.println(movementLine);
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByBlack.get(position).remove(movementLine);
            }
            movementLine.findAttackedPositions();
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByBlack.get(position).add(movementLine);
            }
        }

        // origin updates
        System.out.println("White lines passing through origin");
        oldMovementLines = new HashSet<>(underAttackByTeam(TeamColor.WHITE).get(origin));
        for (MovementLine movementLine : oldMovementLines) {
            System.out.println(movementLine);
            movementLine.findAttackedPositions();
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByWhite.get(position).add(movementLine);
            }
        }
        System.out.println("Black lines passing through origin");
        oldMovementLines = new HashSet<>(underAttackByTeam(TeamColor.BLACK).get(origin));
        for (MovementLine movementLine : oldMovementLines) {
            System.out.println(movementLine);
            movementLine.findAttackedPositions();
            for (ChessPosition position : movementLine.getAttackedPositions()) {
                underAttackByBlack.get(position).add(movementLine);
            }
        }

        findAllValidMoves();
        teamTurn = enemyTeam(teamTurn);
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
        movementLinesByOrigin = board.getMovementLines();
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

        for (Collection<MovementLine> movementLines : movementLinesByOrigin.values()){
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

            return moves;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }
}
