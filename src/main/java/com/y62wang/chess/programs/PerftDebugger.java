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
        if (args.length == 3)
        {
            runs = Integer.parseInt(args[2]);
        }
        for (int i = 0; i < runs; i++)
        {
            Bitboard.MAKE_MOVE_TIME = 0;
            Bitboard.LEGAL_MOVE_TIME = 0;
            Perft.singlePerftNonTest(board, depth, 0);

            if (Bitboard.MAKE_MOVE_TIME != 0 || Bitboard.LEGAL_MOVE_TIME != 0)
            {
                System.out.println(String.format("make_move:   %10s ms", Bitboard.MAKE_MOVE_TIME / 1000));
                System.out.println(String.format("legal_moves: %10s ms", Bitboard.LEGAL_MOVE_TIME / 1000));
            }

        }
    }
}
