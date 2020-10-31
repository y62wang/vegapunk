package com.y62wang.chess.engine.search;

import com.y62wang.chess.engine.Move;

public class MoveSorter
{
    public void sort(short[] moves)
    {
        // insertion sort .. for now
        for (int i = 0; i < moves.length; i++)
        {
            for (int j = i; j > 0; j--)
            {
                if (compare(moves[j], moves[j - 1]) >= 0)
                {
                    break;
                }

                // swap
                short temp = moves[j];
                moves[j] = moves[j-1];
                moves[j-1] = temp;
            }
        }
    }

    public int compare(short moveA, short moveB)
    {
        return Move.moveCode(moveA) - Move.moveCode(moveB);
    }

}
