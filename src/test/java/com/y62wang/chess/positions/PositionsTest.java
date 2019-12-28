package com.y62wang.chess.positions;

import com.y62wang.chess.Bitboard;
import org.junit.Test;

import static com.y62wang.chess.perft.Perft.validatePositions;

public class PositionsTest
{
    @Test
    public void testCastlePosition()
    {
        long[] tests = new long[] {27, 718, 19459, 518277, 14240699};
        Bitboard board = new Bitboard("r3k2r/p3p2p/8/4B3/4b3/8/P3P2P/R3K2R w KQkq -");
        validatePositions(board, tests);
    }

    @Test
    public void testPawnPromotion()
    {
        long[] tests = new long[] {30, 690, 16911};
        Bitboard board = new Bitboard("4knnr/1P4P1/8/8/8/8/1p4p1/4KNNR w KQkq -");
        validatePositions(board, tests);
    }

    @Test
    public void testEnpassant()
    {
        long[] tests = new long[] {6, 50, 402, 3552};
        Bitboard board = new Bitboard("4k3/1p1p4/8/2P5/8/8/8/4K3 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testPin()
    {
        long[] tests = new long[] {4, 92, 2071};
        Bitboard board = new Bitboard("4k3/4r3/8/8/8/2b1B3/3R4/4K3 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testDoublePin()
    {
        long[] tests = new long[] {4, 92, 2071, 47510};
        Bitboard board = new Bitboard("4k3/4r3/8/8/8/2b1B3/3R4/4K3 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testCheck()
    {
        long[] tests = new long[] {6, 80, 914};
        Bitboard board = new Bitboard("4k3/4r3/2N5/8/8/8/8/4K3 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testDoubleCheck()
    {
        long[] tests = new long[] {3, 69, 730};
        Bitboard board = new Bitboard("4k3/4r3/2N5/8/1b6/8/8/4K3 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testKingWalk()
    {
        long[] tests = new long[] {1, 50, 153};
        Bitboard board = new Bitboard("3qk3/7b/8/4n3/8/4K3/r7/8 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testBehtingStudy()
    {
        long[] tests = new long[] {25, 180, 4098, 46270};
        Bitboard board = new Bitboard("8/8/7p/3KNN1k/2p4p/8/3P2p1/8 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testDjajaStudy()
    {
        long[] tests = new long[] {32, 657, 18238, 419717};
        Bitboard board = new Bitboard("6R1/P2k4/r7/5N1P/r7/p7/7K/8 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testHakmem70()
    {
        long[] tests = new long[] {18, 27, 524, 1347};
        Bitboard board = new Bitboard("5B2/6P1/1p6/8/1N6/kP6/2K5/8 w - -");
        validatePositions(board, tests);
    }

    @Test
    public void testPawnDoublePushCheckCanBeCapturedByEP()
    {
        long[] tests = new long[] {7, 57, 397};
        Bitboard board = new Bitboard("4k3/2p5/8/3P4/3K4/8/8/8 b - - 0 1");
        validatePositions(board, tests);
    }
}
