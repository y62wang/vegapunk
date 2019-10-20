package com.y62wang.chess;

import com.y62wang.chess.magic.MagicCache;

public class InBetweenCache
{

    private static InBetweenCache INSTANCE;

    private final long[][] cache;

    private InBetweenCache()
    {
        cache = precompute();
    }

    public synchronized static InBetweenCache getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new InBetweenCache();
        }
        return INSTANCE;
    }

    public long inBetweenSet(int fromSquare, int toSquare)
    {
        return cache[fromSquare][toSquare];
    }

    private long[][] precompute()
    {
        MagicCache magicCache = MagicCache.getInstance();
        long[][] inBetweenCache = new long[Bitboard.SIZE][Bitboard.SIZE];
        for (int from = 0; from < Bitboard.SIZE; from++)
        {
            for (int to = 0; to < Bitboard.SIZE; to++)
            {
                long inBetweenSet = 0;
                long toBB = BoardUtil.squareBB(to);
                long fromBB = BoardUtil.squareBB(from);

                long fromRookAttacks = magicCache.rookAttacks(from, toBB);
                long toRookAttacks = magicCache.rookAttacks(to, fromBB);

                if ((fromRookAttacks & toBB) != 0 && (toRookAttacks & fromBB) != 0)
                {
                    inBetweenSet |= (fromRookAttacks & toRookAttacks);
                }

                long fromBishopAttacks = magicCache.bishopAttacks(from, toBB);
                long toBishopAttacks = magicCache.bishopAttacks(to, fromBB);

                if ((fromBishopAttacks & toBB) != 0 && (toBishopAttacks & fromBB) != 0)
                {
                    inBetweenSet |= (fromBishopAttacks & toBishopAttacks);
                }

                inBetweenCache[from][to] = inBetweenSet;
            }
        }
        return inBetweenCache;
    }
}
