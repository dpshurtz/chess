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
    private final HashMap<ChessPosition, Collection<MovementLine>> underAttackByWhite = new HashMap<>();
    private final HashMap<ChessPosition, Collection<MovementLine>> underAttackByBlack = new HashMap<>();
    private ChessPosition kingPosWhite;
    private ChessPosition kingPosBlack;
    private final HashMap<ChessPosition, Collection<ChessMove>> validMovesFrom = new HashMap<>();
    private boolean canCastleWhiteQ = true;
    private boolean canCastleWhiteK = true;
    private boolean canCastleBlackQ = true;
    private boolean canCastleBlackK = true;
    private ChessPosition enPassantSquare = null;

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
        ChessPiece.PieceType type = piece.getPieceType();

        movementLinesByOrigin.get(origin).clear();
        movementLinesByOrigin.put(destination, piece.getMovementLines(board, destination));

        enPassantSquare = null;

        if (type == ChessPiece.PieceType.KING) {
            if (teamTurn == TeamColor.WHITE) {
                canCastleWhiteQ = false;
                canCastleWhiteK = false;
                kingPosWhite = destination;
            }
            else {
                canCastleBlackQ = false;
                canCastleBlackK = false;
                kingPosBlack = destination;
            }
        }

        else if (type == ChessPiece.PieceType.ROOK) {
            if (teamTurn == TeamColor.WHITE) {
                if (origin.getColumn() == 1) {
                    canCastleWhiteQ = false;
                }
                else if (origin.getColumn() == 8) {
                    canCastleWhiteK = false;
                }
            }
            else {
                if (origin.getColumn() == 1) {
                    canCastleBlackQ = false;
                }
                else if (origin.getColumn() == 8) {
                    canCastleBlackK = false;
                }
            }
        }

        else if (type == ChessPiece.PieceType.PAWN) {
            if (origin.getRow() == 2 && destination.getRow() == 4) {
                enPassantSquare = new ChessPosition(3, origin.getColumn());
            }
            else if (origin.getRow() == 7 && destination.getRow() == 5) {
                enPassantSquare = new ChessPosition(6, origin.getColumn());
            }
        }

        resetAllAttacks();
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
        return (isInCheck(teamColor) && hasNoValidMoves(teamColor));
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return (!isInCheck(teamColor) && hasNoValidMoves(teamColor));
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        movementLinesByOrigin = board.getMovementLines();

        ChessPiece piece;
        piece = board.getPiece(new ChessPosition(1, 5));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING || piece.getTeamColor() != TeamColor.WHITE) {
            canCastleWhiteQ = false;
            canCastleWhiteK = false;
        }
        piece = board.getPiece(new ChessPosition(1, 1));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != TeamColor.WHITE) {
            canCastleWhiteQ = false;
        }
        piece = board.getPiece(new ChessPosition(1, 8));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != TeamColor.WHITE) {
            canCastleWhiteK = false;
        }
        piece = board.getPiece(new ChessPosition(8, 5));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING || piece.getTeamColor() != TeamColor.BLACK) {
            canCastleBlackQ = false;
            canCastleBlackK = false;
        }
        piece = board.getPiece(new ChessPosition(8, 1));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != TeamColor.BLACK) {
            canCastleBlackQ = false;
        }
        piece = board.getPiece(new ChessPosition(8, 8));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != TeamColor.BLACK) {
            canCastleBlackK = false;
        }

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

    private boolean hasNoValidMoves(TeamColor team) {
        ChessPiece piece;
        for (ChessPosition position : validMovesFrom.keySet()) {
            piece = board.getPiece(position);
            if (piece != null && piece.getTeamColor() == team && !validMovesFrom.get(position).isEmpty()) {
                return false;
            }
        }
        return true;
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
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            moves.removeIf(move -> !isSafe(move.getEndPosition(), team));

            ChessPosition[] castlePos = new ChessPosition[2];
            if (team == TeamColor.WHITE) {
                if (canCastleWhiteQ){
                    castlePos[0] = new ChessPosition(1, 4);
                    castlePos[1] = new ChessPosition(1, 3);
                    if (!isInCheck(team) &&
                            board.getPiece(castlePos[0]) == null && isSafe(castlePos[0], team) &&
                            board.getPiece(castlePos[1]) == null && isSafe(castlePos[1], team)
                    ) {
                        moves.add(new ChessMove(startPosition, castlePos[1], null));
                    }
                }
                if (canCastleWhiteK) {
                    castlePos[0] = new ChessPosition(1, 6);
                    castlePos[1] = new ChessPosition(1, 7);
                    if (!isInCheck(team) &&
                            board.getPiece(castlePos[0]) == null && isSafe(castlePos[0], team) &&
                            board.getPiece(castlePos[1]) == null && isSafe(castlePos[1], team)
                    ) {
                        moves.add(new ChessMove(startPosition, castlePos[1], null));
                    }
                }
            }
            else {
                if (canCastleBlackQ) {
                    castlePos[0] = new ChessPosition(8, 4);
                    castlePos[1] = new ChessPosition(8, 3);
                    if (!isInCheck(team) &&
                            board.getPiece(castlePos[0]) == null && isSafe(castlePos[0], team) &&
                            board.getPiece(castlePos[1]) == null && isSafe(castlePos[1], team)
                    ) {
                        moves.add(new ChessMove(startPosition, castlePos[1], null));
                    }
                }
                if (canCastleBlackK) {
                    castlePos[0] = new ChessPosition(8, 6);
                    castlePos[1] = new ChessPosition(8, 7);
                    if (!isInCheck(team) &&
                            board.getPiece(castlePos[0]) == null && isSafe(castlePos[0], team) &&
                            board.getPiece(castlePos[1]) == null && isSafe(castlePos[1], team)
                    ) {
                        moves.add(new ChessMove(startPosition, castlePos[1], null));
                    }
                }
            }
            return moves;
        }

        else {
            if (enPassantSquare != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                int diffToEnPassantSquare = enPassantSquare.getColumn() - startPosition.getColumn();
                if ((diffToEnPassantSquare == 1 || diffToEnPassantSquare == -1) &&
                        ((team == TeamColor.WHITE && startPosition.getRow() == 5) ||
                                (team == TeamColor.BLACK && startPosition.getRow() == 4))) {
                    moves.add(new ChessMove(startPosition, enPassantSquare, null));
                }
            }

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
