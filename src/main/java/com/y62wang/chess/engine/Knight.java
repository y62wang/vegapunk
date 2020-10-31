package com.y62wang.chess.engine;

import static com.y62wang.chess.engine.BoardConstants.BOARD_SIZE;

public class Knight
{
    private static long[] ATTACKS = new long[BOARD_SIZE];

    static
    {
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int rank = BoardUtil.rank(square);
            int file = BoardUtil.file(square);
            long atks = 0;

            if (BoardUtil.isWithinDimension(rank + 2) && BoardUtil.isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank + 2);
            if (BoardUtil.isWithinDimension(rank + 2) && BoardUtil.isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank + 2);
            if (BoardUtil.isWithinDimension(rank + 1) && BoardUtil.isWithinDimension(file + 2)) atks |= BoardUtil.squareBB(file + 2, rank + 1);
            if (BoardUtil.isWithinDimension(rank + 1) && BoardUtil.isWithinDimension(file - 2)) atks |= BoardUtil.squareBB(file - 2, rank + 1);
            if (BoardUtil.isWithinDimension(rank - 1) && BoardUtil.isWithinDimension(file + 2)) atks |= BoardUtil.squareBB(file + 2, rank - 1);
            if (BoardUtil.isWithinDimension(rank - 1) && BoardUtil.isWithinDimension(file - 2)) atks |= BoardUtil.squareBB(file - 2, rank - 1);
            if (BoardUtil.isWithinDimension(rank - 2) && BoardUtil.isWithinDimension(file - 1)) atks |= BoardUtil.squareBB(file - 1, rank - 2);
            if (BoardUtil.isWithinDimension(rank - 2) && BoardUtil.isWithinDimension(file + 1)) atks |= BoardUtil.squareBB(file + 1, rank - 2);

            ATTACKS[square] = atks;
        }
    }

    public static long targets(int square)
    {
        return ATTACKS[square];
    }
}
