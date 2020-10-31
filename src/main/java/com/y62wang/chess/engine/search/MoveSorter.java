package com.y62wang.chess.engine.search;

import com.y62wang.chess.engine.Move;

public class MoveSorter
{
    public void sort(final short[] moves)
    {
        // insertion sort .. for now
        for (int i = 0; i < moves.length; i++)
        {
            for (int j = i; j > 0; j--)
            {
                if (this.compare(moves[j], moves[j - 1]) >= 0)
                {
                    break;
                }

                // swap
                final short temp = moves[j];
                moves[j] = moves[j-1];
                moves[j-1] = temp;
            }
        }
    }

    public int compare(final short moveA, final short moveB)
    {
        return Move.moveCode(moveA) - Move.moveCode(moveB);
    }

}
