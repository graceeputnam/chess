package client;

import chess.*;
import ui.EscapeSequences;

public class BoardDrawer {

    public static void draw(boolean isBlack) {
        var board = new ChessBoard();
        board.resetBoard();
        System.out.println();
        if (isBlack) {
            drawBoard(board, true);
        } else {
            drawBoard(board, false);
        }
        System.out.println();
    }

    private static void drawBoard(ChessBoard board, boolean isBlack) {
        String[] cols = isBlack ? new String[]{"h","g","f","e","d","c","b","a"} :
                new String[]{"a","b","c","d","e","f","g","h"};
        int[] rows = isBlack ? new int[]{1,2,3,4,5,6,7,8} :
                new int[]{8,7,6,5,4,3,2,1};

        printColHeaders(cols);
        for (int row : rows) {
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + row + " ");
            for (String col : cols) {
                int colNum = col.charAt(0) - 'a' + 1;
                boolean isLight = (row + colNum) % 2 != 0;
                String bg = isLight ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREEN;
                System.out.print(bg + getPieceString(board, row, colNum));
            }
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + row + " ");
            System.out.print(EscapeSequences.RESET_BG_COLOR + "\n");
        }
        printColHeaders(cols);
    }

    private static void printColHeaders(String[] cols) {
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_COLOR_WHITE + "   ");
        for (String col : cols) {
            System.out.print(" " + col + " ");
        }
        System.out.print("   " + EscapeSequences.RESET_BG_COLOR + "\n");
    }

    private static String getPieceString(ChessBoard board, int row, int col) {
        var piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        String color = isWhite ? EscapeSequences.SET_TEXT_COLOR_RED : EscapeSequences.SET_TEXT_COLOR_BLUE;
        return color + getPieceSymbol(piece);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        return switch (piece.getPieceType()) {
            case KING -> isWhite ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> isWhite ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> isWhite ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> isWhite ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> isWhite ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> isWhite ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }
}