package com.y62wang.chess.engine.search;

public class TranspositionEntry
{
    short bestMove;
    double score;
    double depth;

    public TranspositionEntry(final short bestMove, final double score, final double depth)
    {
        this.bestMove = bestMove;
        this.score = score;
        this.depth = depth;
    }
}
