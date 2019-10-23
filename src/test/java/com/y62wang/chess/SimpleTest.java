package com.y62wang.chess;

import org.junit.Test;

import static com.y62wang.chess.perft.Perft.perftTime;

public class SimpleTest
{
    @Test
    public void test1() {
//        long[] tests = new long[] {48, 2039, 97862, 4085603, 193690690};
//        Bitboard startingBoard = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        long[] tests = new long[] {20, 400, 8902, 197281, 4865609, 119060324};
        Bitboard startingBoard = new Bitboard();
        System.out.println(startingBoard);
        startingBoard.debug();
        perftTime(startingBoard,5);
    }
}
