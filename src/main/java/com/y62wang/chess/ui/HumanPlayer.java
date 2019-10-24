package com.y62wang.chess.ui;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
import com.y62wang.chess.programs.CommandLineGame;

import java.util.Scanner;

public class HumanPlayer
{
    public static Bitboard makeHumanMove(Bitboard board, Scanner scanner)
    {
        short[] moves = board.legalMoves();
        System.out.print("Enter a move: ");
        String line = scanner.nextLine();
        if (line.equals("cccc"))
        {
            CommandLineGame.makeComputerMove(board);
        }
        else if (line.length() != 4)
        {
            System.out.println("Invalid move: " + line);
            return board;
        }
        short tempMove = Move.of(line.substring(0, 2), line.substring(2, 4));
        boolean invalidMove = true;
        for (short move : moves)
        {
            if (Move.fromSquare(tempMove) == Move.fromSquare(move) && Move.toSquare(tempMove) == Move.toSquare(move))
            {
                return board.makeMove(move);
            }
        }
        if (invalidMove)
        {
            System.out.println("Invalid move: " + line);
            return board;
        }
        return board;
    }
}
