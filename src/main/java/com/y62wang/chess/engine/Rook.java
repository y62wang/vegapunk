package com.y62wang.chess.engine;

import static com.y62wang.chess.engine.BoardConstants.BOARD_DIM;
import static com.y62wang.chess.engine.BoardConstants.FILE_A;
import static com.y62wang.chess.engine.BoardConstants.FILE_H;
import static com.y62wang.chess.engine.BoardConstants.RANK_1;
import static com.y62wang.chess.engine.BoardConstants.RANK_8;

public class Rook
{
    public static long rookMask(int square)
    {
        long mask = 0;

        // right
        for (int index = 0; BoardUtil.file(square + index) < FILE_H; index++)
        {
            mask |= BoardUtil.squareBB(square + index);
        }

        // left
        for (int index = 0; BoardUtil.file(square - index) > FILE_A; index++)
        {
            mask |= BoardUtil.squareBB(square - index);
        }

        // up
        for (int index = 0; BoardUtil.rank(square + index * BOARD_DIM) < RANK_8; index++)
        {
            mask |= BoardUtil.squareBB(square + index * BOARD_DIM);
        }

        // down
        for (int index = 0; BoardUtil.rank(square - index * BOARD_DIM) > RANK_1; index++)
        {
            mask |= BoardUtil.squareBB(square - index * BOARD_DIM);
        }

        mask &= ~BoardUtil.squareBB(square);
        return mask;
    }

    public static long rookAttacks(int square, long innerOccupied)
    {
        long attackSet = 0;
        long sq = 1L << square;

        int rank = BoardUtil.rank(square);

        // right
        for (int index = 0; index + square < (1 + rank) * BOARD_DIM; index++)
        {
            attackSet |= (sq << index);
            if (0 != (innerOccupied & (sq << index))) break;
        }

        // left
        for (int index = 0; square - index >= rank * BOARD_DIM; index++)
        {
            attackSet |= (sq >>> index);
            if (0 != (innerOccupied & (sq >>> index))) break;
        }

        // up
        for (int index = 0; square + index * BOARD_DIM < 64; index++)
        {
            attackSet |= (sq << (index * 8));
            if (0 != (innerOccupied & (sq << (index * 8)))) break;
        }

        // down
        for (int index = 0; square - index * BOARD_DIM >= 0; index++)
        {
            attackSet |= (sq >>> (index * 8));
            if (0 != (innerOccupied & (sq >>> (index * 8)))) break;
        }

        attackSet &= ~BoardUtil.squareBB(square);
        return attackSet;
    }
}
