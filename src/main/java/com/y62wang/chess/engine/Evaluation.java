package com.y62wang.chess.engine;

import com.y62wang.chess.engine.bits.PopulationCount;
import com.y62wang.chess.engine.enums.PieceType;
import com.y62wang.chess.engine.enums.Side;

import java.util.Random;

public class Evaluation
{
    private static final Random random = new Random(0);

    public static double evaluate(Bitboard bb)
    {
        return evaluate(bb, Side.WHITE) - evaluate(bb, Side.BLACK);
    }

    public static double evaluateAbsolute(Bitboard bb)
    {
        return evaluate(bb, bb.getTurn()) - evaluate(bb, bb.getTurn().flip());
    }

    private static double evaluate(Bitboard bb, Side side)
    {
        int moveCount = bb.legalMoves().length;
        if (moveCount == 0)
        {
            return -1000;
        }

        long k = bb.getPieceList().piecesBB(side, PieceType.KING);
        long q = bb.getPieceList().piecesBB(side, PieceType.QUEEN);
        long r = bb.getPieceList().piecesBB(side, PieceType.ROOK);
        long b = bb.getPieceList().piecesBB(side, PieceType.BISHOP);
        long n = bb.getPieceList().piecesBB(side, PieceType.KNIGHT);
        long p = bb.getPieceList().piecesBB(side, PieceType.PAWN);

        double pawnScores = 0;
        if (side == Side.WHITE)
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
        double v = PopulationCount.popCount(k) * 200
                   + PopulationCount.popCount(q) * 9
                   + PopulationCount.popCount(r) * 5
                   + PopulationCount.popCount(b) * 3
                   + PopulationCount.popCount(n) * 3
                   + PopulationCount.popCount(p) * 1
                   + PopulationCount.popCount((targets)) * 0.05
                   + pawnScores
                   + moveCount * 0.1;
        return v;
    }

    public static void main(String[] args)
    {
        Bitboard bitboard = new Bitboard();
        System.out.println(evaluate(bitboard));
    }
}
