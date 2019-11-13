package com.y62wang.chess;

import com.y62wang.chess.search.AlphaBeta.Node;

public class SomeTest
{
    public static void main(String[] args)
    {
        Bitboard bb = new Bitboard();
        TranspositionTable<Node> tt = new TranspositionTable<>();
        long hash = tt.hash(bb);
        bb.makeMove(Move.of("e2e4"));
        long hash2 = tt.hash(bb);
        bb.unmake();
        long hash3 = tt.hash(bb);

        System.out.println(hash);
        System.out.println(hash2);
        System.out.println(hash3);
    }
}
