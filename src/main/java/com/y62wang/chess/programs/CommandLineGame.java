package com.y62wang.chess.programs;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
import com.y62wang.chess.ui.HumanPlayer;

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
                board = HumanPlayer.makeHumanMove(board, scanner);
                continue;
            }
            else
            {
                board = makeComputerMove(board);
            }
            moveCount++;
        }
    }

    public static Bitboard makeComputerMove(final Bitboard board)
    {
        Random random = new Random(1L);
        List<Short> whiteLegal = board.legalMoves().stream().collect(Collectors.toList());
        Short move = whiteLegal.get(random.nextInt(whiteLegal.size()));
        System.out.println("Computer made move " + Move.moveString(move));
        return board.makeMove(move);
    }

}
