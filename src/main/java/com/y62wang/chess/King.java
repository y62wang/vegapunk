package com.y62wang.chess;

import com.y62wang.chess.bits.BitScan;

import static com.y62wang.chess.BoardConstants.BOARD_SIZE;
import static com.y62wang.chess.BoardUtil.isWithinDimension;

public class King
{
    private static final long[] ATTACKS = new long[BOARD_SIZE];

    static
    {
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int rank = BoardUtil.rank(square);
            int file = BoardUtil.file(square);
            long atks = 0;

            if (isWithinDimension(rank + 1) && isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank + 1);
            if (isWithinDimension(rank + 1) && isWithinDimension(file + 0)) atks |= BoardUtil.squareBB(file + 0, rank + 1);
            if (isWithinDimension(rank + 1) && isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank + 1);
            if (isWithinDimension(rank + 0) && isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank + 0);
            if (isWithinDimension(rank + 0) && isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank + 0);
            if (isWithinDimension(rank - 1) && isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank - 1);
            if (isWithinDimension(rank - 1) && isWithinDimension(file + 0)) atks |= BoardUtil.squareBB(file + 0, rank - 1);
            if (isWithinDimension(rank - 1) && isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank - 1);

            ATTACKS[square] = atks;
        }
    }

    public static long kingTargets(int square)
    {
        return ATTACKS[square];
    }
}
