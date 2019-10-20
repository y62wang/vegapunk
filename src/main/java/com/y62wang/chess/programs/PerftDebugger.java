package com.y62wang.chess.programs;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
import com.y62wang.chess.perft.Perft;

public class PerftDebugger
{
    public static void main(String[] args)
    {
        Bitboard board = new Bitboard(args[0]);
        int depth = Integer.parseInt(args[1]);
        Perft.singlePerft(board, depth, 0);
    }

    private static Bitboard makeMoves(Bitboard board, String... moves)
    {
        for (final String move : moves)
        {
            board = board.makeMove(Move.of(move));
        }
        return board;
    }
}
