package com.y62wang.chess.programs;

import com.y62wang.chess.Bitboard;

import java.util.Scanner;

import static com.y62wang.chess.ui.ComputerPlayer.makeComputerMove;
import static com.y62wang.chess.ui.HumanPlayer.makeHumanMove;

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
            int legalMoveCount = moves.length;
            if (legalMoveCount == 0)
            {
                System.out.println("Game over!");
                System.out.println(board);
                break;
            }

            if (board.isWhiteTurn())
            {
                makeHumanMove(board, scanner);
                continue;
            }
            else
            {
                makeComputerMove(board);
            }
            moveCount++;
        }
    }
}
