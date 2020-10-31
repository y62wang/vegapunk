package com.y62wang.chess.engine.search;

public class SearchNode
{
    public short move;
    public double score;

    public SearchNode(short move, double score)
    {
        this.move = move;
        this.score = score;
    }
}
