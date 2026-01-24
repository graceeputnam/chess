package chess;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor teamColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return teamColor == that.teamColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, type);
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor(){
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if (board == null || myPosition == null) {
            return java.util.Collections.emptyList();
        }

        PieceMovesCalculator calculator;

        //pick the right calculator for each piece
        if (type == PieceType.KING) {
            calculator = new KingMovesCalculator(teamColor);
        } else if (type == PieceType.QUEEN) {
            calculator = new QueenMovesCalculator(teamColor);
        } else if (type == PieceType.BISHOP) {
            calculator = new BishopMovesCalculator(teamColor);
        } else if (type == PieceType.KNIGHT) {
            calculator = new KnightMovesCalculator(teamColor);
        } else if (type == PieceType.ROOK) {
            calculator = new RookMovesCalculator(teamColor);
        } else {
            calculator = new PawnMovesCalculator(teamColor);
        }

        return calculator.calcMoves(board, myPosition);
    }
    }
