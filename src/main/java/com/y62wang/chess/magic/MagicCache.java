package com.y62wang.chess.magic;

import com.y62wang.chess.Bishop;
import com.y62wang.chess.BoardUtil;
import com.y62wang.chess.Rook;
import com.y62wang.chess.Util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.y62wang.chess.BoardConstants.BB_RANK_2;
import static com.y62wang.chess.BoardConstants.BOARD_SIZE;
import static com.y62wang.chess.magic.MagicConstants.BISHOP_MAGIC;
import static com.y62wang.chess.magic.MagicConstants.BISHOP_MAGIC_BITS;
import static com.y62wang.chess.magic.MagicConstants.ROOK_MAGIC;
import static com.y62wang.chess.magic.MagicConstants.ROOK_MAGIC_BITS;

public class MagicCache
{
    private static MagicCache INSTANCE;

    private final Magic[] bishopMagic;
    private final Magic[] rookMagic;

    static
    {
        INSTANCE = new MagicCache();
    }

    public static synchronized MagicCache getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new MagicCache();
        }
        return INSTANCE;
    }

    public MagicCache()
    {
        rookMagic = prepareMagic(ROOK_MAGIC, ROOK_MAGIC_BITS, Rook::rookMask, Rook::rookAttacks);
        bishopMagic = prepareMagic(BISHOP_MAGIC, BISHOP_MAGIC_BITS, Bishop::bishopMask, Bishop::bishopAttacks);
    }

    private Magic[] prepareMagic(long[] magicNumbers, int[] magicBits, Function<Integer, Long> maskFunction, BiFunction<Integer, Long, Long> attackFunction)
    {
        Magic[] magicCache = new Magic[BOARD_SIZE];
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int bitShifts = BOARD_SIZE - magicBits[square];
            long magicNumber = magicNumbers[square];
            long mask = maskFunction.apply(square);
            long[] attacks = indexAttacksCache(square, magicBits[square], magicNumber, mask, attackFunction);
            Magic magic = new Magic(bitShifts, magicNumber, mask, attacks);
            magicCache[square] = magic;
        }
        return magicCache;
    }

    private long[] indexAttacksCache(final int square, final int magicBits, final long magicNumber, final long mask, BiFunction<Integer, Long, Long> attackFunction)
    {
        List<Integer> squares = BoardUtil.squaresOfBB(mask);
        long[] attacks = new long[( int ) Math.pow(2, magicBits)];
        for (int encodedCombination = 0; encodedCombination < ( int ) Math.pow(2, squares.size()); encodedCombination++)
        {
            long occupancy = getOccupiedSquares(squares, encodedCombination);
            long attackSet = attackFunction.apply(square, occupancy);
            int key = ( int ) ((occupancy * magicNumber) >>> (64 - magicBits));
            attacks[key] = attackSet;
        }
        return attacks;
    }

    /**
     * Create a BB with specific occupancy squares.
     * The occupancyBits indicates which of the square should be set.
     *
     * @param occupiedSquares squares that could be set
     * @param occupancyBits   bit at position N indicates the square occupied.get(N) should bet set
     * @return BB with set occupancies
     */
    private static long getOccupiedSquares(List<Integer> occupiedSquares, long occupancyBits)
    {
        long result = 0;
        for (int i = 0; i < 32; i++)
        {
            if (((1L << i) & occupancyBits) != 0)
            {

                result |= (1L << occupiedSquares.get(i));
            }
        }
        return result;
    }

    public long rookAttacks(int square, long occupied)
    {
        Magic magic = rookMagic[square];
        return magic.getAttacks(occupied);
    }

    public long bishopAttacks(int square, long occupied)
    {
        Magic magic = bishopMagic[square];
        return magic.getAttacks(occupied);
    }

    public long queenAttacks(int square, long occupied)
    {
        return bishopAttacks(square, occupied) | rookAttacks(square, occupied);
    }

    public static void main(String[] args)
    {
        MagicCache magicCache = new MagicCache();
        long pos = BoardUtil.position(4) | BB_RANK_2;
        long result = magicCache.rookAttacks(0, pos);
        Util.printBitboard(result);
    }
}
