package com.y62wang.chess.perft;

import com.google.common.base.Stopwatch;
import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
import com.y62wang.chess.Util;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Perft
{
    public static void singlePerftNonTest(Bitboard board, int depth, long expectedNodeCount)
    {
        Map<Integer, Long> map = new HashMap<>();
        Map<String, Integer> roots = new HashMap<>();
        perftTime(board, depth);
        roots.forEach((key, val) -> System.out.println(String.format("%s %s", key, val)));
    }

    public static void singlePerft(Bitboard board, int depth, long expectedNodeCount)
    {
        Map<Integer, Long> map = new HashMap<>();
        Map<String, Integer> roots = new HashMap<>();
        perft(board, depth, 0, map, roots, ( short ) 0);
        System.out.println(roots.keySet() + " " + roots.values());
        System.out.println("==============================================================");
        // roots.forEach((key, val) -> System.out.println(String.format("%s %s", key, val)));
        Assert.assertEquals(Long.valueOf(expectedNodeCount), map.getOrDefault(depth, 0L));
    }

    public static void perft(Bitboard startingBoard, long[] expectedNodeCountByDepth)
    {
        for (int index = 0; index < expectedNodeCountByDepth.length; index++)
        {
            singlePerft(startingBoard, index + 1, expectedNodeCountByDepth[index]);
        }
    }

    public static void perftTime(Bitboard board, int depth)
    {
        Stopwatch stopwatch = Stopwatch.createStarted();
        long result = perftPure(board, depth, 0);
        stopwatch.stop();
        long millis = stopwatch.elapsed().toMillis();
        System.out.println(String.format("perft(%s) %6s ms %10s nodes %7s nodes/ms", depth, millis, result, result / millis));
    }

    private static long perftPure(Bitboard startingBoard, int targetDepth, int currentDepth)
    {
        if (currentDepth == targetDepth)
        {
            return 1;
        }
        long result = 0;
        for (short possibleMove : startingBoard.legalMoves())
        {
            Bitboard board = startingBoard.makeMove(possibleMove);
            if (currentDepth == 0)
            {
                board.rootMove = possibleMove;
            }
            else
            {
                board.rootMove = startingBoard.rootMove;
            }
            result = result + perftPure(board, targetDepth, currentDepth + 1);
        }
        return result;
    }

    private static void perft(Bitboard startingBoard, int targetDepth, int currentDepth, Map<Integer, Long> nodes, Map<String, Integer> roots, short lastMove)
    {
        if (currentDepth == targetDepth)
        {
            nodes.putIfAbsent(targetDepth, 0L);
            nodes.put(targetDepth, nodes.get(targetDepth) + 1);
            String key = Move.moveString(startingBoard.rootMove);
            // String key = startingBoard.rootMove.toString() + "-" + startingBoard.rootMove.getFlag();
            roots.putIfAbsent(key, 0);
            roots.put(key, roots.get(key) + 1);
            return;
        }
        Set<Short> legalMoves = startingBoard.legalMoves();
//        String repeated = new String(new char[currentDepth]).replace("\0", "    ");
//        System.out.print(repeated + "> " + legalMoves.size() + " ");
//        Util.printMoves(legalMoves);
        for (short possibleMove : legalMoves)
        {
//            System.out.println(repeated + " Making move " + Move.moveString(possibleMove));
            Bitboard board = startingBoard.makeMove(possibleMove);
            if (currentDepth == 0)
            {
                board.rootMove = possibleMove;
            }
            else
            {
                board.rootMove = startingBoard.rootMove;
            }
            perft(board, targetDepth, currentDepth + 1, nodes, roots, possibleMove);
        }
    }
}
