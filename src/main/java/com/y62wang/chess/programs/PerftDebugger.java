package com.y62wang.chess.programs;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.perft.Perft;

public class PerftDebugger
{
    public static void main(String[] args)
    {
        Bitboard board = new Bitboard(args[0]);
        int depth = Integer.parseInt(args[1]);
        int runs = 1;
        if(args.length == 3) {
            runs = Integer.parseInt(args[2]);
        }
        for (int i = 0; i < runs; i++)
        {
            Perft.singlePerftNonTest(board, depth, 0);
        }
    }
}
