package com.y62wang.chess.perft;

import com.google.common.base.Stopwatch;
import com.y62wang.chess.Bitboard;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class Perft
{
    public static void singlePerft(Bitboard board, int depth, long expectedNodeCount)
    {
        Map<Integer, Long> map = new HashMap<>();
        Map<String, Integer> roots = new HashMap<>();
        long result = timedPerft(board, depth);
        System.out.println(roots.keySet() + " " + roots.values());
        System.out.println("==============================================================");
        Assert.assertEquals(Long.valueOf(expectedNodeCount), Long.valueOf(result));
    }

    public static void validatedPerfts(Bitboard startingBoard, long[] expectedNodeCountByDepth)
    {
        for (int index = 0; index < expectedNodeCountByDepth.length; index++)
        {
            singlePerft(startingBoard, index + 1, expectedNodeCountByDepth[index]);
        }
    }

    public static long timedPerft(Bitboard board, int depth)
    {
        Stopwatch stopwatch = Stopwatch.createStarted();
        long result = perft(board, depth);
        stopwatch.stop();
        long millis = stopwatch.elapsed().toMillis();
        System.out.println(String.format("perft(%s) %6s ms %10s nodes %7s nodes/ms", depth, millis, result, millis == 0 ? 0 : result / millis));
        return result;
    }

    private static long perft(Bitboard startingBoard, int depth)
    {
        if (depth==1)
        {
            return startingBoard.legalMoves().size();
        }

        long result = 0;
        for (short possibleMove : startingBoard.legalMoves())
        {
            Bitboard board = startingBoard.makeMove(possibleMove);
            result = result + perft(board, depth - 1);
        }
        return result;
    }
}
