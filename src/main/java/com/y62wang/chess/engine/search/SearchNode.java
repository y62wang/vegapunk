package com.y62wang.chess.engine.search;

public class SearchNode
{
    public short move;
    public double score;

    public SearchNode(final short move, final double score)
    {
        this.move = move;
        this.score = score;
    }
}
