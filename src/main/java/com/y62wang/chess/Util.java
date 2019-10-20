package com.y62wang.chess;

import com.y62wang.chess.bits.Endianess;

import java.util.Collection;
import java.util.stream.IntStream;

import static com.y62wang.chess.Bitboard.BOARD_WIDTH;
import static com.y62wang.chess.Bitboard.SIZE;

public class Util
{
    public static String bitboardString(long board)
    {
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, SIZE)
                .map(i -> Endianess.toLittleEndian(i))
                .forEach(i ->
                         {
                             sb.append(0 != (board & (1L << i)) ? '*' : '_');
                             sb.append(' ');
                             if ((i + 1) % BOARD_WIDTH == 0)
                             {
                                 sb.append("\n");
                             }
                         });
        return sb.toString();
    }

    public static void printBitboard(long board)
    {
        System.out.println(bitboardString(board));
    }

    public static void printMoves(Collection<Short> moves)
    {
        for (final Short move : moves)
        {
            System.out.print(Move.moveString(move) + " ");
        }
        System.out.println();
    }

    public static void printMoves2(Collection<Short> moves)
    {
        for (final Short move : moves)
        {
            System.out.print(Move.moveString(move) + "(" + move + ") ");
        }
        System.out.println();
    }
}
