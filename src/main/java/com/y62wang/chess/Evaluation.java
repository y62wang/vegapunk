package com.y62wang.chess;

import com.y62wang.chess.bits.PopulationCount;
import com.y62wang.chess.enums.Side;

import java.util.Random;

import static com.y62wang.chess.enums.PieceType.BISHOP;
import static com.y62wang.chess.enums.PieceType.KING;
import static com.y62wang.chess.enums.PieceType.KNIGHT;
import static com.y62wang.chess.enums.PieceType.PAWN;
import static com.y62wang.chess.enums.PieceType.QUEEN;
import static com.y62wang.chess.enums.PieceType.ROOK;
import static com.y62wang.chess.enums.Side.BLACK;
import static com.y62wang.chess.enums.Side.WHITE;

public class Evaluation
{
    private static final Random random = new Random(0);

    public static double evaluate(Bitboard bb)
    {
        double val = evaluate(bb, WHITE) - evaluate(bb, BLACK);
//        System.out.println(bb);
//        System.out.println(val);
//        System.out.println("===========================");
        return val;
    }

    public static double evaluateAbsolute(Bitboard bb)
    {
        double val = evaluate(bb, bb.getTurn()) - evaluate(bb, bb.getTurn().flip());
//        System.out.println(bb);
//        System.out.println(val);
//        System.out.println("===========================");

        return val;
    }

    private static double evaluate(Bitboard bb, Side side)
    {
        int moveCount = bb.legalMoves().length;
        if (moveCount == 0)
        {
            return -1000;
        }

        long k = bb.getPieceList().piecesBB(side, KING);
        long q = bb.getPieceList().piecesBB(side, QUEEN);
        long r = bb.getPieceList().piecesBB(side, ROOK);
        long b = bb.getPieceList().piecesBB(side, BISHOP);
        long n = bb.getPieceList().piecesBB(side, KNIGHT);
        long p = bb.getPieceList().piecesBB(side, PAWN);

        double pawnScores = 0;
        if (side == WHITE)
        {
            pawnScores += PopulationCount.popCount(p & BoardConstants.RANK_7) * 0.05;
            pawnScores += PopulationCount.popCount(p & BoardConstants.RANK_6) * 0.02;
            pawnScores += PopulationCount.popCount(p & (BoardConstants.RANK_5 | BoardConstants.RANK_4)) * 0.01;
        }
        else
        {
            pawnScores += PopulationCount.popCount(p & BoardConstants.RANK_2) * 0.05;
            pawnScores += PopulationCount.popCount(p & BoardConstants.RANK_3) * 0.02;
            pawnScores += PopulationCount.popCount(p & (BoardConstants.RANK_4 | BoardConstants.RANK_5)) * 0.01;
        }
        long targets = bb.targets(side);
        long opponentTargets = bb.targets(side.flip());
        double v = PopulationCount.popCount(k) * 200
                   + PopulationCount.popCount(q) * 9
                   + PopulationCount.popCount(r) * 5
                   + PopulationCount.popCount(b) * 3
                   + PopulationCount.popCount(n) * 3
                   + PopulationCount.popCount(p) * 1
                   + PopulationCount.popCount((targets)) * 0.05
                   + pawnScores
                   + moveCount * 0.1;
//                   + PopulationCount.popCount((targets & bb.getPieceList().piecesBB(Piece.of(side.flip(), KING)))) * 0.1
//                   - PopulationCount.popCount(opponentTargets & bb.getPieceList().piecesBB(side, QUEEN)) * 0.2
//                   - PopulationCount.popCount(opponentTargets & bb.getPieceList().piecesBB(side, KING)) * 0.3
//                   + PopulationCount.popCount(BoardConstants.CENTER_BOARD & targets) * 0.05;
//                   + PopulationCount.popCount(targets & bb.pieces(side.flip())) * 0.01;
//                   - PopulationCount.popCount(~targets & bb.pieces(side)) * 0.11;
        // + random.nextDouble() / 1000000;
        return v;
    }

    public static void main(String[] args)
    {
        Bitboard bitboard = new Bitboard();
        System.out.println(evaluate(bitboard));
    }
}
