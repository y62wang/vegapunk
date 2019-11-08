package com.y62wang.chess.perft;

import com.google.common.base.Stopwatch;
import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
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

    public static void validatePositions(Bitboard startingBoard, long[] expectedNodeCountByDepth)
    {
        for (int index = 0; index < expectedNodeCountByDepth.length; index++)
        {
            singlePerft(startingBoard, index + 1, expectedNodeCountByDepth[index]);
        }
    }

    public static long timedPerft(Bitboard board, int depth)
    {
        Stopwatch stopwatch = Stopwatch.createStarted();
        long result = perft(new Bitboard(board), depth);
        stopwatch.stop();
        long millis = stopwatch.elapsed().toMillis();
        System.out.println(String.format("perft(%s) %6s ms %10s nodes %7s nodes/ms", depth, millis, result, millis == 0 ? 0 : result / millis));
        return result;
    }

    public static void divide(Bitboard board, int depth)
    {
        Map<String, Integer> moveCounter = new HashMap<>();
        long result = divide(board, depth, moveCounter, null);
        moveCounter.forEach((key, val) -> System.out.println(String.format("%s %s", key, val)));
        System.out.println("Total nodes: " + result);
    }

    public static long divide(Bitboard board, int depth, Map<String, Integer> moveCounter, String rootMove)
    {
        short[] moves = board.legalMoves();
        if (depth == 1)
        {
            if (rootMove != null)
            {
                moveCounter.put(rootMove, moves.length + moveCounter.get(rootMove));
            }
            return moves.length;
        }
        long result = 0;
        int moveIndex = 0;
        while (moveIndex < moves.length)
        {
            String moveString = Move.moveString(moves[moveIndex]);
            if (rootMove == null)
            {
                moveCounter.put(moveString, 0);
            }
            board.makeMove(moves[moveIndex]);
            result = result + divide(board, depth - 1, moveCounter, rootMove == null ? moveString : rootMove);
            board.unmake();
            moveIndex++;
        }
        return result;
    }

    private static long perft(Bitboard startingBoard, int depth)
    {
        short[] moves = startingBoard.legalMoves();
        if (depth == 1)
        {
            return moves.length;
        }

        long result = 0;
        int moveIndex = 0;
        while (moveIndex < moves.length)
        {
            startingBoard.makeMove(moves[moveIndex]);
            result = result + perft(startingBoard, depth - 1);
            startingBoard.unmake();
            moveIndex++;
        }
        return result;
    }
}
