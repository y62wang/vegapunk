package com.y62wang.chess.game.ui;

import com.google.common.base.Stopwatch;
import com.y62wang.chess.engine.Bitboard;
import com.y62wang.chess.engine.Move;
import com.y62wang.chess.engine.TranspositionTable;
import com.y62wang.chess.engine.search.AlphaBeta;
import com.y62wang.chess.engine.search.SearchAlgorithm;
import com.y62wang.chess.engine.search.SearchNode;

import java.time.Duration;

public class ComputerPlayer
{
    public static void makeComputerMove(Bitboard board)
    {
        Stopwatch start = Stopwatch.createStarted();
        SearchAlgorithm searchAlgorithm = new AlphaBeta(new TranspositionTable<>());
        SearchNode node = searchAlgorithm.search(board, 4 - (board.isWhiteTurn() ? 0 : 1));
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
