package com.y62wang.chess.engine.util;

import com.y62wang.chess.engine.Move;
import com.y62wang.chess.engine.bits.Endianess;

import java.util.Collection;
import java.util.stream.IntStream;

import static com.y62wang.chess.engine.Bitboard.BOARD_WIDTH;
import static com.y62wang.chess.engine.Bitboard.SIZE;

public class BitboardUtil
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
        for (Short move : moves)
        {
            System.out.print(Move.moveString(move) + " ");
        }
        System.out.println();
    }
}
