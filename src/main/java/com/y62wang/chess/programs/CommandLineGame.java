package com.y62wang.chess.programs;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

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
            Set<Short> moves = board.legalMoves();
            if (moves.size() == 0)
            {
                System.out.println("Game over!");
                System.out.println(board);
                break;
            }

            if (board.isWhiteTurn())
            {
                board = makeHumanMove(board, scanner);
                continue;
            }
            else
            {
                board = makeComputerMove(board);
            }
            moveCount++;
        }
    }

    private static Bitboard makeComputerMove(final Bitboard board)
    {
        Random random = new Random(1L);
        List<Short> whiteLegal = board.legalMoves().stream().collect(Collectors.toList());
        Short move = whiteLegal.get(random.nextInt(whiteLegal.size()));
        System.out.println("Computer made move " + Move.moveString(move));
        return board.makeMove(move);
    }

    private static Bitboard makeHumanMove(Bitboard board, Scanner scanner)
    {
        Set<Short> moves = board.legalMoves();
        System.out.print("Enter a move: ");
        String line = scanner.nextLine();
        if (line.equals("cccc"))
        {
            makeComputerMove(board);
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
