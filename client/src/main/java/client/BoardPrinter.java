package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.EscapeSequences;

import java.util.Collection;
import java.util.HashSet;

public class BoardPrinter {
    public static String getColumnString(int col) {
        return switch (col) {
            case 1 -> EscapeSequences.WIDE_A;
            case 2 -> EscapeSequences.WIDE_B;
            case 3 -> EscapeSequences.WIDE_C;
            case 4 -> EscapeSequences.WIDE_D;
            case 5 -> EscapeSequences.WIDE_E;
            case 6 -> EscapeSequences.WIDE_F;
            case 7 -> EscapeSequences.WIDE_G;
            case 8 -> EscapeSequences.WIDE_H;
            default -> EscapeSequences.EMPTY;
        };
    }

    public static String getRowString(int row) {
        return switch (row) {
            case 1 -> EscapeSequences.WIDE_1;
            case 2 -> EscapeSequences.WIDE_2;
            case 3 -> EscapeSequences.WIDE_3;
            case 4 -> EscapeSequences.WIDE_4;
            case 5 -> EscapeSequences.WIDE_5;
            case 6 -> EscapeSequences.WIDE_6;
            case 7 -> EscapeSequences.WIDE_7;
            case 8 -> EscapeSequences.WIDE_8;
            default -> EscapeSequences.EMPTY;
        };
    }

    public static String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        String pieceString;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            switch (piece.getPieceType()) {
                case KING -> pieceString = EscapeSequences.WHITE_KING;
                case QUEEN -> pieceString = EscapeSequences.WHITE_QUEEN;
                case BISHOP -> pieceString = EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> pieceString = EscapeSequences.WHITE_KNIGHT;
                case ROOK -> pieceString = EscapeSequences.WHITE_ROOK;
                case PAWN -> pieceString = EscapeSequences.WHITE_PAWN;
                default -> pieceString = EscapeSequences.EMPTY;
            }
            pieceString = EscapeSequences.SET_TEXT_COLOR_WHITE + pieceString;
        }
        else {
            switch (piece.getPieceType()) {
                case KING -> pieceString = EscapeSequences.BLACK_KING;
                case QUEEN -> pieceString = EscapeSequences.BLACK_QUEEN;
                case BISHOP -> pieceString = EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> pieceString = EscapeSequences.BLACK_KNIGHT;
                case ROOK -> pieceString = EscapeSequences.BLACK_ROOK;
                case PAWN -> pieceString = EscapeSequences.BLACK_PAWN;
                default -> pieceString = EscapeSequences.EMPTY;
            }
            pieceString = EscapeSequences.SET_TEXT_COLOR_BLACK + pieceString;
        }
        return pieceString;
    }

    public static void displayGame(ChessGame.TeamColor team, ChessBoard board) {
        displayGame(team, null, new HashSet<>(), board);
    }

    public static void displayGame(ChessGame.TeamColor team, ChessPosition origin,
                             Collection<ChessPosition> highlightPositions, ChessBoard board) {
        if (board == null) {
            return;
        }

        int[] rows, cols;
        if (team == ChessGame.TeamColor.BLACK) {
            rows = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
            cols = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        }
        else {
            rows = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
            cols = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        }

        boolean onBorder;
        for (int row : rows) {
            onBorder = row == 0 || row == 9;

            for (int col : cols) {
                if (onBorder) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
                    System.out.print(getColumnString(col));
                }
                else if (col == 0 || col == 9 ) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
                    System.out.print(getRowString(row));
                }
                else {
                    ChessPosition position = new ChessPosition(row, col);
                    ChessPiece piece = board.getPiece(position);
                    boolean isOrigin = position.equals(origin);
                    boolean isHighlighted = highlightPositions.contains(position);

                    setBackground(position, isOrigin, isHighlighted);
                    System.out.print(getPieceString(piece));
                }
            }

            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    public static void setBackground(ChessPosition position, boolean isOrigin, boolean isHighlighted) {
        if (isOrigin) {
            System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
        }

        else if ((position.getRow() + position.getColumn())%2 == 1) {
            if (isHighlighted) {
                System.out.print(EscapeSequences.SET_BG_COLOR_GREEN);
            }
            else {
                System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            }
        }
        else {
            if (isHighlighted) {
                System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
            }
            else {
                System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
            }
        }
    }
}
