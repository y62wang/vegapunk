package com.y62wang.chess.engine;

import org.junit.Assert;
import org.junit.Test;

public class TranspositionTableTest
{
    @Test
    public void testHashForDifferentCastleRights() {
        Bitboard bb1 = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        Bitboard bb2 = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w - -");
        TranspositionTable<Integer> table = new TranspositionTable<>();
        Assert.assertNotEquals(table.hash(bb1), table.hash(bb2));
    }

    @Test
    public void testHashForDifferentEPFile() {
        Bitboard bb1 = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq 5");
        Bitboard bb2 = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq 8");
        TranspositionTable<Integer> table = new TranspositionTable<>();
        Assert.assertNotEquals(table.hash(bb1), table.hash(bb2));
    }
}
