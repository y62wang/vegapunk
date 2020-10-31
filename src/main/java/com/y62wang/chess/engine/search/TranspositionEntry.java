package com.y62wang.chess.engine.search;

public class TranspositionEntry
{
    short bestMove;
    double score;
    double depth;

    public TranspositionEntry(short bestMove, double score, double depth)
    {
        this.bestMove = bestMove;
        this.score = score;
        this.depth = depth;
    }
}
