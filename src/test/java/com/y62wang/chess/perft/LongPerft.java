package com.y62wang.chess.perft;

import com.y62wang.chess.Bitboard;
import org.junit.Test;

import static com.y62wang.chess.perft.Perft.timedPerft;

public class LongPerft
{
    @Test
    public void testPerftKiwipete()
    {
        Bitboard startingBoard = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        //Bitboard startingBoard = new Bitboard();
        timedPerft(startingBoard,10);
    }
}
