package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board = new ChessBoard();
    private TeamColor teamTurn = TeamColor.WHITE;

    // Maps to track which squares are attacked along which lines
    private HashMap<ChessPosition, Collection<MovementLine>> movementLinesByOrigin;
    private final HashMap<ChessPosition, Collection<MovementLine>> underAttackByWhite = new HashMap<>();
    private final HashMap<ChessPosition, Collection<MovementLine>> underAttackByBlack = new HashMap<>();

    // Store precalculated valid moves
    private final HashMap<ChessPosition, Collection<ChessMove>> validMovesFrom = new HashMap<>();

    // Track king positions to determine check
    private ChessPosition kingPosWhite;
    private ChessPosition kingPosBlack;

    // Values indicating validity of special moves
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

        // Update the board with the move
        board.makeMove(move);

        ChessPosition origin = move.getStartPosition();
        ChessPosition destination = move.getEndPosition();
        ChessPiece.PieceType type = piece.getPieceType();

        // Find new movement lines available to the piece after it is moved
        movementLinesByOrigin.get(origin).clear();
        movementLinesByOrigin.put(destination, piece.getMovementLines(board, destination));

        // Update king position
        if (type == ChessPiece.PieceType.KING) {
            if (teamTurn == TeamColor.WHITE) {
                kingPosWhite = destination;
            }
            else {
                kingPosBlack = destination;
            }
        }

        // Update validity of special moves
        updateCastlingValidity(move, type);
        updateEnPassantValidity(move, type);

        // Update attacked squares and precalculate valid moves
        resetAllAttacks();
        findAllValidMoves();

        teamTurn = enemyTeam(teamTurn);
    }

    /**
     * Check if a moved piece was a king or rook and
     * update castling validity accordingly
     *
     * @param move chess move that was performed
     * @param pieceType type of piece that was moved
     */
    private void updateCastlingValidity(ChessMove move, ChessPiece.PieceType pieceType) {
        ChessPosition origin = move.getStartPosition();
        ChessPosition destination = move.getEndPosition();

        // If a king moves, it is no longer allowed to castle
        if (pieceType == ChessPiece.PieceType.KING) {
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

        // If a rook moves, it is no longer allowed to castle
        else if (pieceType == ChessPiece.PieceType.ROOK) {
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
    }

    /**
     * Check if a moved piece was a pawn moving two squares
     * then mark the square it jumped as an en passant target for the next turn
     *
     * @param move chess move that was performed
     * @param pieceType type of piece that was moved
     */
    private void updateEnPassantValidity(ChessMove move, ChessPiece.PieceType pieceType) {
        ChessPosition origin = move.getStartPosition();
        ChessPosition destination = move.getEndPosition();

        // Reset en passant validity
        enPassantSquare = null;
        if (pieceType == ChessPiece.PieceType.PAWN) {
            // If a pawn moves two squares, it may be taken by en passant
            if (origin.getRow() == 2 && destination.getRow() == 4) {
                enPassantSquare = new ChessPosition(3, origin.getColumn());
            }
            else if (origin.getRow() == 7 && destination.getRow() == 5) {
                enPassantSquare = new ChessPosition(6, origin.getColumn());
            }
        }
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
        reinitializeCastlingValidity();
        resetAllAttacks();
        setKingPositions();
        findAllValidMoves();
    }

    /**
     * Checks if rooks and kings are in their starting positions
     * and disables their ability to castle if they are not
     */
    private void reinitializeCastlingValidity() {
        ChessPiece piece;

        // Check white king's starting position
        piece = board.getPiece(new ChessPosition(1, 5));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING || piece.getTeamColor() != TeamColor.WHITE) {
            canCastleWhiteQ = false;
            canCastleWhiteK = false;
        }

        // Check white rooks' starting positions
        piece = board.getPiece(new ChessPosition(1, 1));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != TeamColor.WHITE) {
            canCastleWhiteQ = false;
        }
        piece = board.getPiece(new ChessPosition(1, 8));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != TeamColor.WHITE) {
            canCastleWhiteK = false;
        }

        // Check black king's starting position
        piece = board.getPiece(new ChessPosition(8, 5));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING || piece.getTeamColor() != TeamColor.BLACK) {
            canCastleBlackQ = false;
            canCastleBlackK = false;
        }

        // Check black rooks' starting positions
        piece = board.getPiece(new ChessPosition(8, 1));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != TeamColor.BLACK) {
            canCastleBlackQ = false;
        }
        piece = board.getPiece(new ChessPosition(8, 8));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != TeamColor.BLACK) {
            canCastleBlackK = false;
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * Find which squares are under attack on which lines
     */
    private void resetAllAttacks() {
        // Clear current attacks on all positions
        for (ChessPosition position : board.getPositions()) {
            underAttackByWhite.put(position, new HashSet<>());
            underAttackByBlack.put(position, new HashSet<>());
        }

        // Check squares along all movement lines
        for (Collection<MovementLine> movementLines : movementLinesByOrigin.values()){
            for (MovementLine movementLine : movementLines) {
                // Update or initialize attacked positions on each line
                movementLine.findAttackedPositions();

                // Add the line to the corresponding map of each square it is threatening
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

    /**
     * Determines if a given square is safe (not under attack)
     *
     * @param position the square to check
     * @param myTeam the player's team
     * @return True if the square is safe
     */
    private boolean isSafe(ChessPosition position, TeamColor myTeam) {
        return underAttackByTeam(enemyTeam(myTeam)).get(position).isEmpty();
    }

    /**
     * Finds the map indicating which squares are under attack by the given team
     *
     * @param team the attacking team
     */
    private HashMap<ChessPosition, Collection<MovementLine>> underAttackByTeam(TeamColor team) {
        if (team == TeamColor.WHITE) {
            return underAttackByWhite;
        }
        else {
            return underAttackByBlack;
        }
    }

    /**
     * Determines what the enemy's team color is
     *
     * @param team the player's team
     */
    private TeamColor enemyTeam(TeamColor team) {
        if (team == TeamColor.WHITE) {
            return TeamColor.BLACK;
        }
        else {
            return TeamColor.WHITE;
        }
    }

    /**
     * Finds what the kings' initial positions are
     */
    private void setKingPositions() {
        ChessPiece piece;
        for (ChessPosition position : board.getPositions()) {
            // Check every square to see if it contains a king
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

    /**
     * @return The position of the king of the specified color
     */
    private ChessPosition kingPos(TeamColor team) {
        if (team == TeamColor.WHITE) {
            return kingPosWhite;
        }
        else {
            return kingPosBlack;
        }
    }

    /**
     * @param team the player's team
     * @return True if the specified team has no valid moves for any piece
     */
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

    /**
     * Precalculates all valid moves for each location and stores in a map
     */
    private void findAllValidMoves() {
        for (ChessPosition position : board.getPositions()) {
            validMovesFrom.put(position, validMovesPrecalculate(position));
        }
    }

    /**
     * Determines which moves are valid for a given piece, also taking into account
     * rules such as check, castling, and en passant
     *
     * @param startPosition the piece's position
     * @return A collection of all moves a piece can make
     * or null if there is no piece at the given location
     */
    private Collection<ChessMove> validMovesPrecalculate(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        TeamColor team = piece.getTeamColor();
        // Load all moves the piece could make without considering check or special movement
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            // Kings cannot move into check
            moves.removeIf(move -> !isSafe(move.getEndPosition(), team));
            addCastlingIfValid(moves, startPosition);
        }

        else {
            addEnPassantIfValid(moves, startPosition);

            // If king is in check, filter moves to block any lines threatening the king
            if (isInCheck(team)) {
                for (MovementLine movementLine : underAttackByTeam(enemyTeam(team)).get(kingPos(team))) {
                    moves.removeIf(move ->
                            !movementLine.getAttackedPositions().contains(move.getEndPosition()) &&
                                    !movementLine.getPositionSequence().getFirst().equals(move.getEndPosition())
                    );
                }
            }

            // If piece is pinned to the king, it cannot leave the pinning line
            for (MovementLine movementLine : underAttackByTeam(enemyTeam(team)).get(startPosition)) {
                if (movementLine.isPinned(startPosition)) {
                    moves.removeIf(move -> !movementLine.getPositionSequence().contains(move.getEndPosition()));
                }
            }

        }
        return moves;
    }

    /**
     * Checks if a king can castle, and adds that as a valid move if it can
     *
     * @param moves the collection of moves to which to add castling (if valid)
     * @param startPosition the king's position
     */
    private void addCastlingIfValid(Collection<ChessMove> moves, ChessPosition startPosition) {
        TeamColor team = board.getPiece(startPosition).getTeamColor();

        ChessPosition[] castlePos = new ChessPosition[2];
        if (team == TeamColor.WHITE) {
            if (canCastleWhiteQ){
                castlePos[0] = new ChessPosition(1, 4);
                castlePos[1] = new ChessPosition(1, 3);
                // King cannot start in check or move through check while castling
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
                // King cannot start in check or move through check while castling
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
                // King cannot start in check or move through check while castling
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
                // King cannot start in check or move through check while castling
                if (!isInCheck(team) &&
                        board.getPiece(castlePos[0]) == null && isSafe(castlePos[0], team) &&
                        board.getPiece(castlePos[1]) == null && isSafe(castlePos[1], team)
                ) {
                    moves.add(new ChessMove(startPosition, castlePos[1], null));
                }
            }
        }
    }

    /**
     * Checks if a piece can perform en passant, and adds that as a valid move if it can
     *
     * @param moves the collection of moves to which to add en passant (if valid)
     * @param startPosition the piece's position
     */
    private void addEnPassantIfValid(Collection<ChessMove> moves, ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        TeamColor team = piece.getTeamColor();

        // Check if the piece is a pawn and if an enemy pawn moved twice last turn
        if (enPassantSquare != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int diffToEnPassantSquare = enPassantSquare.getColumn() - startPosition.getColumn();
            // To perform en passant, the pawn must be on the same row
            // as the enemy pawn that moved twice, one column away
            if ((diffToEnPassantSquare == 1 || diffToEnPassantSquare == -1) &&
                    ((team == TeamColor.WHITE && startPosition.getRow() == 5) ||
                            (team == TeamColor.BLACK && startPosition.getRow() == 4))) {
                moves.add(new ChessMove(startPosition, enPassantSquare, null));
            }
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
