package chess;

import java.util.Collection;

/**
 * A class that holds and updates information about
 * castling and en passant validity
 */
public class SpecialMoveValidator {

    private boolean canCastleWhiteQ = true;
    private boolean canCastleWhiteK = true;
    private boolean canCastleBlackQ = true;
    private boolean canCastleBlackK = true;
    private ChessPosition enPassantSquare = null;

    public SpecialMoveValidator() {
    }

    /**
     * Check if a moved piece was a king or rook and
     * update castling validity accordingly
     *
     * @param move chess move that was performed
     * @param piece the piece that was moved
     */
    public void updateCastlingValidity(ChessMove move, ChessPiece piece) {
        ChessPosition origin = move.getStartPosition();

        // If a king moves, it is no longer allowed to castle
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                canCastleWhiteQ = false;
                canCastleWhiteK = false;
            }
            else {
                canCastleBlackQ = false;
                canCastleBlackK = false;
            }
        }

        // If a rook moves, it is no longer allowed to castle
        else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
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
     * @param piece the piece that was moved
     */
    public void updateEnPassantValidity(ChessMove move, ChessPiece piece) {
        ChessPosition origin = move.getStartPosition();
        ChessPosition destination = move.getEndPosition();

        // Reset en passant validity
        enPassantSquare = null;
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
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
     * Checks if rooks and kings are in their starting positions
     * and disables their ability to castle if they are not
     */
    public void reinitializeCastlingValidity(ChessBoard board) {
        ChessPiece piece;

        // Check white king's starting position
        piece = board.getPiece(new ChessPosition(1, 5));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING || piece.getTeamColor() != ChessGame.TeamColor.WHITE) {
            canCastleWhiteQ = false;
            canCastleWhiteK = false;
        }

        // Check white rooks' starting positions
        piece = board.getPiece(new ChessPosition(1, 1));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != ChessGame.TeamColor.WHITE) {
            canCastleWhiteQ = false;
        }
        piece = board.getPiece(new ChessPosition(1, 8));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != ChessGame.TeamColor.WHITE) {
            canCastleWhiteK = false;
        }

        // Check black king's starting position
        piece = board.getPiece(new ChessPosition(8, 5));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING || piece.getTeamColor() != ChessGame.TeamColor.BLACK) {
            canCastleBlackQ = false;
            canCastleBlackK = false;
        }

        // Check black rooks' starting positions
        piece = board.getPiece(new ChessPosition(8, 1));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != ChessGame.TeamColor.BLACK) {
            canCastleBlackQ = false;
        }
        piece = board.getPiece(new ChessPosition(8, 8));
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.ROOK || piece.getTeamColor() != ChessGame.TeamColor.BLACK) {
            canCastleBlackK = false;
        }
    }

    /**
     * Checks if a king can castle, and adds that as a valid move if it can
     *
     * @param moves the collection of moves to which to add castling (if valid)
     * @param startPosition the king's position
     * @param game the current chess game
     */
    public void addCastlingIfValid(Collection<ChessMove> moves, ChessPosition startPosition, ChessGame game) {
        ChessBoard board = game.getBoard();
        ChessGame.TeamColor team = board.getPiece(startPosition).getTeamColor();

        ChessPosition[] castlePos = new ChessPosition[2];
        if (team == ChessGame.TeamColor.WHITE) {
            if (canCastleWhiteQ){
                castlePos[0] = new ChessPosition(1, 4);
                castlePos[1] = new ChessPosition(1, 3);
                // King cannot start in check or move through check while castling
                if (!game.isInCheck(team) &&
                        board.getPiece(castlePos[0]) == null && game.isSafe(castlePos[0], team) &&
                        board.getPiece(castlePos[1]) == null && game.isSafe(castlePos[1], team)
                ) {
                    moves.add(new ChessMove(startPosition, castlePos[1], null));
                }
            }
            if (canCastleWhiteK) {
                castlePos[0] = new ChessPosition(1, 6);
                castlePos[1] = new ChessPosition(1, 7);
                // King cannot start in check or move through check while castling
                if (!game.isInCheck(team) &&
                        board.getPiece(castlePos[0]) == null && game.isSafe(castlePos[0], team) &&
                        board.getPiece(castlePos[1]) == null && game.isSafe(castlePos[1], team)
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
                if (!game.isInCheck(team) &&
                        board.getPiece(castlePos[0]) == null && game.isSafe(castlePos[0], team) &&
                        board.getPiece(castlePos[1]) == null && game.isSafe(castlePos[1], team)
                ) {
                    moves.add(new ChessMove(startPosition, castlePos[1], null));
                }
            }
            if (canCastleBlackK) {
                castlePos[0] = new ChessPosition(8, 6);
                castlePos[1] = new ChessPosition(8, 7);
                // King cannot start in check or move through check while castling
                if (!game.isInCheck(team) &&
                        board.getPiece(castlePos[0]) == null && game.isSafe(castlePos[0], team) &&
                        board.getPiece(castlePos[1]) == null && game.isSafe(castlePos[1], team)
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
     * @param game the current chess game
     */
    public void addEnPassantIfValid(Collection<ChessMove> moves, ChessPosition startPosition, ChessGame game) {
        ChessPiece piece = game.getBoard().getPiece(startPosition);
        ChessGame.TeamColor team = piece.getTeamColor();

        // Check if the piece is a pawn and if an enemy pawn moved twice last turn
        if (enPassantSquare != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int diffToEnPassantSquare = enPassantSquare.getColumn() - startPosition.getColumn();
            // To perform en passant, the pawn must be on the same row
            // as the enemy pawn that moved twice, one column away
            if ((diffToEnPassantSquare == 1 || diffToEnPassantSquare == -1) &&
                    ((team == ChessGame.TeamColor.WHITE && startPosition.getRow() == 5) ||
                            (team == ChessGame.TeamColor.BLACK && startPosition.getRow() == 4))) {
                moves.add(new ChessMove(startPosition, enPassantSquare, null));
            }
        }
    }
}
