package com.y62wang.chess.ui;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
import com.y62wang.chess.enums.PieceType;

import java.util.Scanner;

import static com.y62wang.chess.ui.ComputerPlayer.makeComputerMove;

public class HumanPlayer
{
    public static void makeHumanMove(Bitboard board, Scanner scanner)
    {
        short[] moves = board.legalMoves();
        System.out.print("Enter a move: ");
        String line = scanner.nextLine();
        if (line == null || line.length() <= 1)
        {
            makeComputerMove(board);
            return;
        }
        else if (line.equals("unmake"))
        {
            board.unmake();
            board.unmake();
        }
        else if (line.length() == 2)
        {
            int boardIndex = Move.boardIndex(line);
            for (final short move : moves)
            {
                if (Move.toSquare(move) == boardIndex && board.getPieceList().onSquare(Move.fromSquare(move)).type == PieceType.PAWN)
                {
                    board.makeMove(move);
                    return;
                }
            }
            System.out.println("Invalid move: " + line);
            return;
        }
        else if (line.length() == 3)
        {
            int boardIndex = Move.boardIndex(line.substring(1));
            for (final short move : moves)
            {
                if (Move.toSquare(move) == boardIndex && board.getPieceList().onSquare(Move.fromSquare(move)).type.pieceChar == Character.toLowerCase(line.charAt(0)))
                {
                    board.makeMove(move);
                    return;
                }
            }
            System.out.println("Invalid move: " + line);
            return;
        }
        else if (line.length() != 4)
        {
            System.out.println("Invalid move: " + line);
            return;
        }
        short tempMove = Move.of(line.substring(0, 2), line.substring(2, 4));
        boolean invalidMove = true;
        for (short move : moves)
        {
            if (Move.fromSquare(tempMove) == Move.fromSquare(move) && Move.toSquare(tempMove) == Move.toSquare(move))
            {
                board.makeMove(move);
                return;
            }
        }
        System.out.println("Invalid move: " + line);
    }
}
