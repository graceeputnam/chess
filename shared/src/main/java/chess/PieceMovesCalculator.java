package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class PieceMovesCalculator {

    protected final ChessGame.TeamColor color;
    public PieceMovesCalculator(ChessGame.TeamColor color){
        this.color = color;
    }
    public abstract Collection<ChessMove>calcMoves(ChessBoard board, ChessPosition start);
    protected boolean onTheBoard(int row, int col){
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    protected void step(ChessBoard board, ChessPosition start, List<ChessMove> moves, int srow, int scol){
        int newRow = start.getRow() + srow;
        int newCol = start.getColumn() + scol;

        if (!onTheBoard(newRow, newCol)){
            return;
        }
        ChessPosition end = new ChessPosition(newRow, newCol);
        ChessPiece target = board.getPiece(end);
        if (target == null || target.getTeamColor() != color){
            moves.add(new ChessMove(start, end, null));
        }
    }

    protected void slide(ChessBoard board, ChessPosition start, List<ChessMove> moves, int srow, int scol){
        int newRow = start.getRow() + srow;
        int newCol = start.getColumn() + scol;

        while (onTheBoard(newRow, newCol)) {
            ChessPosition end = new ChessPosition(newRow, newCol);
            ChessPiece target = board.getPiece(end);
            if (target == null) {
                moves.add(new ChessMove(start, end, null));
            }
            else{
                if (target.getTeamColor() != color){
                    moves.add(new ChessMove(start, end, null));
                }
                break;
            }
        newRow += srow;
        newCol += scol;
        }
    }
}

class KingMovesCalculator extends PieceMovesCalculator {
    public KingMovesCalculator(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public Collection<ChessMove> calcMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        //all 8 surrounding squares
        for (int r = -1; r <= 1; r++) {
            for (int c = -1; c <= 1; c++) {
                if (r == 0 && c == 0) {
                    continue;
                }
                step(board, start, moves, r, c);
            }
        }
        return moves;
    }
}

class QueenMovesCalculator extends PieceMovesCalculator {
    public QueenMovesCalculator(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public Collection<ChessMove> calcMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        int[][]dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
        };

        for (int[] d : dirs){
            slide(board, start, moves, d[0], d[1]);
        }
        return moves;
    }
}

class RookMovesCalculator extends PieceMovesCalculator {
    public RookMovesCalculator(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public Collection<ChessMove> calcMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        int[][]dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        for (int[] d : dirs){
            slide(board, start, moves, d[0], d[1]);
        }
        return moves;
    }
}

class BishopMovesCalculator extends PieceMovesCalculator {
    public BishopMovesCalculator(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public Collection<ChessMove> calcMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        int[][]dirs = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] d : dirs){
            slide(board, start, moves, d[0], d[1]);
        }
        return moves;
    }
}

class KnightMovesCalculator extends PieceMovesCalculator {
    public KnightMovesCalculator(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public Collection<ChessMove> calcMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        int[][] jumps = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2},
        };

        int row = start.getRow();
        int col = start.getColumn();

        for (int[] j : jumps) {
            int newRow = row + j[0];
            int newCol = col + j[1];
            if (!onTheBoard(newRow, newCol)) {
                continue;
            }

            ChessPosition end = new ChessPosition(newRow, newCol);
            ChessPiece target = board.getPiece(end);
            if (target == null || target.getTeamColor() != color) {
                moves.add(new ChessMove(start, end, null));
            }
        }
        return moves;
    }
}

class PawnMovesCalculator extends PieceMovesCalculator{
    public PawnMovesCalculator(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public Collection<ChessMove> calcMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        int row = start.getRow();
        int col = start.getColumn();

        int dir = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (color == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // going forward just 1
        int oneRow = row + dir;
        if (onTheBoard(oneRow, col)) {
            ChessPosition onePos = new ChessPosition(oneRow, col);
            ChessPiece front = board.getPiece(onePos);
            if (front == null) {
                addPawnMoveWithPromotion(start, onePos, moves, promotionRow);

                // 2 steps from the start row
                if (row == startRow) {
                    int twoRow = row + 2 * dir;
                    if (onTheBoard(twoRow, col)){
                        ChessPosition twoPos = new ChessPosition(twoRow, col);
                        ChessPiece twoFront = board.getPiece(twoPos);
                        if (twoFront == null){
                            moves.add(new ChessMove(start, twoPos, null));
                        }
                    }
                }
            }
        }
        // when the pawn moves diagonal
        int [] gotCol = {col - 1, col + 1};
        for (int c : gotCol) {
            int gotRow = row + dir;
            if (!onTheBoard(gotRow, c)){
                continue;
            }
            ChessPosition gotPos = new ChessPosition(gotRow, c);
            ChessPiece target = board.getPiece(gotPos);
            if (target != null && target.getTeamColor() != color) {
                addPawnMoveWithPromotion(start, gotPos, moves, promotionRow);
            }
        }
        return moves;
    }

    private void addPawnMoveWithPromotion(ChessPosition start, ChessPosition end, List<ChessMove> moves, int promotionRow){
        int endRow = end.getRow();
        // different types of promotion pieces
        if (endRow == promotionRow){
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        }
        else{
            moves.add(new ChessMove(start, end, null));
        }
    }
}

