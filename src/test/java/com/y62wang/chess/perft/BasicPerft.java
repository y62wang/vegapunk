package com.y62wang.chess.perft;

import com.y62wang.chess.Bitboard;
import org.junit.Test;

import static com.y62wang.chess.perft.Perft.perft;

public class BasicPerft
{

    @Test
    public void testPerftInitial()
    {
        long[] tests = new long[] {20, 400, 8902, 197281, 4865609, 119060324};
        Bitboard startingBoard = new Bitboard();
        perft(startingBoard, tests);
    }
}
