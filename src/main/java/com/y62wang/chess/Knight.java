package com.y62wang.chess;

import java.util.stream.IntStream;

import static com.y62wang.chess.BoardConstants.BOARD_DIM;
import static com.y62wang.chess.BoardConstants.BOARD_SIZE;
import static com.y62wang.chess.BoardUtil.isWithinDimension;

public class Knight
{
    private static final long[] ATTACKS = new long[BOARD_SIZE];

    private static final int[] directions = new int[] {7, 8, 9, -1, 1, -7, -8, -9};

    static
    {
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int rank = BoardUtil.rank(square);
            int file = BoardUtil.file(square);
            long atks = 0;

            if (isWithinDimension(rank + 2) && isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank + 2);
            if (isWithinDimension(rank + 2) && isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank + 2);
            if (isWithinDimension(rank + 1) && isWithinDimension(file + 2)) atks |= BoardUtil.squareBB(file + 2, rank + 1);
            if (isWithinDimension(rank + 1) && isWithinDimension(file - 2)) atks |= BoardUtil.squareBB(file - 2, rank + 1);
            if (isWithinDimension(rank - 1) && isWithinDimension(file + 2)) atks |= BoardUtil.squareBB(file + 2, rank - 1);
            if (isWithinDimension(rank - 1) && isWithinDimension(file - 2)) atks |= BoardUtil.squareBB(file - 2, rank - 1);
            if (isWithinDimension(rank - 2) && isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank - 2);
            if (isWithinDimension(rank - 2) && isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank - 2);

            ATTACKS[square] = atks;
        }
    }

    public static long knightAttacks(int square)
    {
        return ATTACKS[square];
    }

}
