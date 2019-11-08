package com.y62wang.chess.programs;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
import com.y62wang.chess.ui.HumanPlayer;

import java.util.Random;
import java.util.Scanner;

public class CommandLineGame
{

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        Bitboard board = new Bitboard();
        if (args.length == 1)
        {
            // FEN
            board = new Bitboard(args[0]);
        }

        int moveCount = 1;
        while (true)
        {
            System.out.println("Move " + moveCount + " Turn: " + (board.isWhiteTurn() ? "WHITE" : "BLACK"));
            System.out.println(board);
            short[] moves = board.legalMoves();
            int legalMoveCount = board.moveCount();
            if (legalMoveCount == 0)
            {
                System.out.println("Game over!");
                System.out.println(board);
                break;
            }

            if (board.isWhiteTurn())
            {
                HumanPlayer.makeHumanMove(board, scanner);
                continue;
            }
            else
            {
                makeComputerMove(board);
            }
            moveCount++;
        }
    }

    public static void makeComputerMove(final Bitboard board)
    {
        Random random = new Random(1L);
        short[] moves = board.legalMoves();
        int moveCount = board.moveCount();
        Short move = moves[random.nextInt(moveCount)];
        System.out.println("Computer made move " + Move.moveString(move));
        board.makeMove(move);
    }

}
