package com.y62wang.chess.engine;

import static com.y62wang.chess.engine.BoardConstants.BOARD_SIZE;

public class King
{
    private static long[] ATTACKS = new long[BOARD_SIZE];

    static
    {
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int rank = BoardUtil.rank(square);
            int file = BoardUtil.file(square);
            long atks = 0;

            if (BoardUtil.isWithinDimension(rank + 1) && BoardUtil.isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank + 1);
            if (BoardUtil.isWithinDimension(rank + 1) && BoardUtil.isWithinDimension(file + 0)) atks |= BoardUtil.squareBB(file + 0, rank + 1);
            if (BoardUtil.isWithinDimension(rank + 1) && BoardUtil.isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank + 1);
            if (BoardUtil.isWithinDimension(rank + 0) && BoardUtil.isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank + 0);
            if (BoardUtil.isWithinDimension(rank + 0) && BoardUtil.isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank + 0);
            if (BoardUtil.isWithinDimension(rank - 1) && BoardUtil.isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank - 1);
            if (BoardUtil.isWithinDimension(rank - 1) && BoardUtil.isWithinDimension(file + 0)) atks |= BoardUtil.squareBB(file + 0, rank - 1);
            if (BoardUtil.isWithinDimension(rank - 1) && BoardUtil.isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank - 1);

            ATTACKS[square] = atks;
        }
    }

    public static long targets(int square)
    {
        return ATTACKS[square];
    }
}
