package com.y62wang.chess.ui;

import com.google.common.base.Stopwatch;
import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
import com.y62wang.chess.search.AlphaBeta;
import com.y62wang.chess.search.AlphaBeta.Node;

import java.time.Duration;
import java.util.Random;

public class ComputerPlayer
{
    public static void makeComputerMove(final Bitboard board)
    {
        Stopwatch start = Stopwatch.createStarted();
        Random random = new Random(1L);
        Node node = AlphaBeta.search(board, 6 - (board.isWhiteTurn() ? 0 : 1));
        if(node == null) {
            System.out.println(board);
            System.out.println("GAME OVER");
            throw new RuntimeException();
        }
        Duration elapsed = start.elapsed();
        short move = node.move;
        System.out.println(String.format("[%s] %s made move '%s' with score %s:", elapsed.toMillis(), board.getTurn(), Move.moveString(move), node.score));
        board.makeMove(move);
    }
}
