package com.y62wang.chess.engine.perft;

import com.y62wang.chess.engine.Bitboard;
import org.junit.Ignore;
import org.junit.Test;

import static com.y62wang.chess.engine.perft.Perft.validatePositions;

public class BasicPerft
{

    @Test
    public void testPerftInitial()
    {
        long[] tests = new long[] {20, 400, 8902, 197281, 4865609, 119060324};
        Bitboard startingBoard = new Bitboard();
        validatePositions(startingBoard, tests);
    }

    @Test
    public void testPerftKiwipete()
    {
        long[] tests = new long[] {48, 2039, 97862, 4085603, 193690690};
        Bitboard startingBoard = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        validatePositions(startingBoard, tests);
    }

    @Test
    @Ignore
    public void testPerftPosition3()
    {
        long[] tests = new long[] {14, 191, 2812, 43238, 674624, 11030083, 178633661};
        char[] boardArray = new char[] {
                ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', 'p', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', ' ', 'p', ' ', ' ', ' ', ' ',
                'K', 'P', ' ', ' ', ' ', ' ', ' ', 'r',
                ' ', 'R', ' ', ' ', ' ', 'p', ' ', 'k',
                ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', ' ', ' ', 'P', ' ', 'P', ' ',
                ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
        };
        Bitboard startingBoard = new Bitboard("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - ");
        validatePositions(startingBoard, tests);
    }

    @Test
    public void testPerftPosition4()
    {
        long[] tests = new long[] {6, 264, 9467, 422333, 15833292}; // 706045033
        char[] boardArray = new char[] {
                'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r',
                'P', 'p', 'p', 'p', ' ', 'p', 'p', 'p',
                ' ', 'b', ' ', ' ', ' ', 'n', 'b', 'N',
                'n', 'P', ' ', ' ', ' ', ' ', ' ', ' ',
                'B', 'B', 'P', ' ', 'P', ' ', ' ', ' ',
                'q', ' ', ' ', ' ', ' ', 'N', ' ', ' ',
                'P', 'p', ' ', 'P', ' ', ' ', 'P', 'P',
                'R', ' ', ' ', 'Q', ' ', 'R', 'K', ' ',
        };
        Bitboard startingBoard = new Bitboard("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
        validatePositions(startingBoard, tests);
    }

    @Test
    public void testPerftPosition5()
    {
        long[] tests = new long[] {44, 1486, 62379, 2103487, 89941194};
        char[] boardArray = new char[] {
                'r', 'n', 'b', 'q', ' ', 'k', ' ', 'r',
                'p', 'p', ' ', 'P', 'b', 'p', 'p', 'p',
                ' ', ' ', 'p', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', 'B', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
                'P', 'P', 'P', ' ', 'N', 'n', 'P', 'P',
                'R', 'N', 'B', 'Q', 'K', ' ', ' ', 'R',

        };
        Bitboard startingBoard = new Bitboard("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
        validatePositions(startingBoard, tests);
    }

    @Test
    public void testPerftPosition6()
    {
        long[] tests = new long[] {46, 2079, 89890, 3894594, 164075551,}; //6923051137L
        char[] boardArray = new char[] {
                'r', ' ', ' ', ' ', ' ', 'r', 'k', ' ',
                ' ', 'p', 'p', ' ', 'q', 'p', 'p', 'p',
                'p', ' ', 'n', 'p', ' ', 'n', ' ', ' ',
                ' ', ' ', 'b', ' ', 'p', ' ', 'B', ' ',
                ' ', ' ', 'B', ' ', 'P', ' ', 'b', ' ',
                'P', ' ', 'N', 'P', ' ', 'N', ' ', ' ',
                ' ', 'P', 'P', ' ', 'Q', 'P', 'P', 'P',
                'R', ' ', ' ', ' ', ' ', 'R', 'K', ' ',

        };
        Bitboard startingBoard = new Bitboard("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10 ");
        validatePositions(startingBoard, tests);
    }

}
