package com.y62wang.chess.engine;

import com.y62wang.chess.engine.search.SearchNode;

public class SomeTest
{
    public static void main(String[] args)
    {
        Bitboard bb = new Bitboard();
        TranspositionTable<SearchNode> tt = new TranspositionTable<>();
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
