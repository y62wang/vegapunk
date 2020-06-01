package com.y62wang.chess.engine;

import static com.y62wang.chess.engine.BoardConstants.FILE_A;
import static com.y62wang.chess.engine.BoardConstants.FILE_H;
import static com.y62wang.chess.engine.BoardConstants.RANK_1;
import static com.y62wang.chess.engine.BoardConstants.RANK_8;

public class Bishop
{
    public static long bishopMask(int square)
    {
        long mask = 0;

        // northeast +9
        for (int sq = square; BoardUtil.rank(sq) < RANK_8 && BoardUtil.file(sq) < FILE_H; sq += 9)
        {
            mask |= BoardUtil.squareBB(sq);
        }

        // northwest +7
        for (int sq = square; BoardUtil.rank(sq) < RANK_8 && BoardUtil.file(sq) > FILE_A; sq += 7)
        {
            mask |= BoardUtil.squareBB(sq);
        }

        // southeast -7
        for (int sq = square; BoardUtil.rank(sq) > RANK_1 && BoardUtil.file(sq) < FILE_H; sq -= 7)
        {
            mask |= BoardUtil.squareBB(sq);
        }

        // southwest -9
        for (int sq = square; BoardUtil.rank(sq) > RANK_1 && BoardUtil.file(sq) > FILE_A; sq -= 9)
        {
            mask |= BoardUtil.squareBB(sq);
        }

        mask &= ~BoardUtil.squareBB(square);
        return mask;
    }

    public static long bishopAttacks(int square, long innerOccupied)
    {
        long attackSet = 0;

        // northeast +9
        for (int sq = square; BoardUtil.rank(sq) <= RANK_8 && BoardUtil.file(sq) <= FILE_H; sq += 9)
        {
            attackSet |= BoardUtil.squareBB(sq);
            if (0 != (innerOccupied & (BoardUtil.squareBB(sq)))) break;
            if (BoardUtil.file(sq) == FILE_H || BoardUtil.rank(sq) == RANK_8) break;
        }

        // northwest +7
        for (int sq = square; BoardUtil.rank(sq) <= RANK_8 && BoardUtil.file(sq) >= FILE_A; sq += 7)
        {
            attackSet |= BoardUtil.squareBB(sq);
            if (0 != (innerOccupied & (BoardUtil.squareBB(sq)))) break;
            if (BoardUtil.file(sq) == FILE_A || BoardUtil.rank(sq) == RANK_8) break;
        }

        // southeast -7
        for (int sq = square; BoardUtil.rank(sq) >= RANK_1 && BoardUtil.file(sq) <= FILE_H; sq -= 7)
        {
            attackSet |= BoardUtil.squareBB(sq);
            if (0 != (innerOccupied & (BoardUtil.squareBB(sq)))) break;
            if (BoardUtil.file(sq) == FILE_H || BoardUtil.rank(sq) == RANK_1) break;
        }

        // southwest -9
        for (int sq = square; BoardUtil.rank(sq) >= RANK_1 && BoardUtil.file(sq) >= FILE_A; sq -= 9)
        {
            attackSet |= BoardUtil.squareBB(sq);
            if (0 != (innerOccupied & (BoardUtil.squareBB(sq)))) break;
            if (BoardUtil.file(sq) == FILE_A || BoardUtil.rank(sq) == RANK_1) break;
        }

        attackSet &= ~BoardUtil.squareBB(square);
        return attackSet;
    }
}
