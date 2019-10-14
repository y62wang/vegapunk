package com.y62wang.chess.magic;

import com.y62wang.chess.Bishop;
import com.y62wang.chess.BoardUtil;
import com.y62wang.chess.Rook;
import com.y62wang.chess.Util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.y62wang.chess.BoardConstants.BB_FILE_D;
import static com.y62wang.chess.BoardConstants.BOARD_SIZE;
import static com.y62wang.chess.magic.MagicConstants.*;

public class MagicCache
{
    private final Magic[] bishopMagic;
    private final Magic[] rookMagic;

    public MagicCache()
    {
        rookMagic = prepareMagic(ROOK_MAGIC, ROOK_MAGIC_BITS, Rook::rookMask, Rook::rookAttacks);
        bishopMagic = prepareMagic(BISHOP_MAGIC, BISHOP_MAGIC_BITS, Bishop::bishopMask, Bishop::bishopAttacks);
    }

    private void prepareBishopMagic()
    {
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int bitShifts = BOARD_SIZE - BISHOP_MAGIC_BITS[square];
            long magicNumber = BISHOP_MAGIC[square];
            long mask = Bishop.bishopMask(square);
            long[] attacks = indexBishopAttacksCache(square, bitShifts, magicNumber, mask);
            Magic magic = new Magic(bitShifts, magicNumber, mask, attacks);
            this.bishopMagic[square] = magic;
        }
    }

    private Magic[] prepareMagic(long[] magicNumbers, int[] magicBits, Function<Integer, Long> maskFunction, BiFunction<Integer, Long, Long> attackSetFunction)
    {
        Magic[] magicCache = new Magic[BOARD_SIZE];
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int bitShifts = BOARD_SIZE - magicBits[square];
            long magicNumber = magicNumbers[square];
            long mask = maskFunction.apply(square);
            long[] attacks = indexAttacksCache(square, magicBits[square], magicNumber, mask, attackSetFunction);
            Magic magic = new Magic(bitShifts, magicNumber, mask, attacks);
            magicCache[square] = magic;
        }
        return magicCache;
    }

    private long[] indexAttacksCache(final int square, final int magicBits, final long magicNumber, final long mask, BiFunction<Integer, Long, Long> attackSetFunction)
    {
        List<Integer> squares = BoardUtil.squaresOfBB(mask);
        long[] attacks = new long[( int ) Math.pow(2, magicBits)];
        for (int j = 0; j < ( int ) Math.pow(2, squares.size()); j++)
        {
            long occupancy = getOccupiedSquares(squares, j);
            long attackSet = attackSetFunction.apply(square, occupancy);
            int key = ( int ) ((occupancy * magicNumber) >>> (64 - magicBits));
            attacks[key] = attackSet;
            System.out.println("Key " + key);
        }
        return attacks;
    }

    private void prepareRookMagic()
    {
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int bitShifts = BOARD_SIZE - ROOK_MAGIC_BITS[square];
            long magicNumber = ROOK_MAGIC[square];
            long mask = Rook.rookMask(square);
            long[] attacks = indexRookAttacksCache(square, bitShifts, magicNumber, mask);
            Magic magic = new Magic(bitShifts, magicNumber, mask, attacks);
            this.rookMagic[square] = magic;
        }
    }

    private long[] indexRookAttacksCache(final int square, final int bitShifts, final long magicNumber, final long mask)
    {
        List<Integer> squares = BoardUtil.squaresOfBB(mask);
        long[] attacks = new long[( int ) Math.pow(2, ROOK_MAGIC_BITS[square])];
        for (int j = 0; j < ( int ) Math.pow(2, squares.size()); j++)
        {
            long occupancy = getOccupiedSquares(squares, j);
            long attackSet = Rook.rookAttacks(square, occupancy);
            int key = ( int ) ((occupancy * magicNumber) >>> bitShifts);
            attacks[key] = attackSet;
        }
        return attacks;
    }

    private long[] indexBishopAttacksCache(final int square, final int bitShifts, final long magicNumber, final long mask)
    {
        List<Integer> squares = BoardUtil.squaresOfBB(mask);
        long[] attacks = new long[( int ) Math.pow(2, BISHOP_MAGIC_BITS[square])];
        for (int j = 0; j < ( int ) Math.pow(2, squares.size()); j++)
        {
            long occupancy = getOccupiedSquares(squares, j);
            long attackSet = Bishop.bishopAttacks(square, occupancy);
            int key = ( int ) ((occupancy * magicNumber) >>> bitShifts);
            attacks[key] = attackSet;
        }
        return attacks;
    }

    /**
     * Create a BB with specific occupancy squares.
     * The occupancyBits indicates which of the square should be set.
     *
     * @param occupiedSquares squares that could be set
     * @param occupancyBits   bit at position N indicates the square occupiedSquares.get(N) should bet set
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

    public long rookAttacks(int square, long blockers)
    {
        Magic magic = rookMagic[square];
        return magic.getAttacks(blockers);
    }

    public long bishopAttacks(int square, long blockers)
    {
        Magic magic = bishopMagic[square];
        return magic.getAttacks(blockers);
    }

    public long getQueenAttacks(int square, long blockers)
    {
        return bishopAttacks(square, blockers) | rookAttacks(square, blockers);
    }

    public static void main(String[] args)
    {
        MagicCache magicCache = new MagicCache();
    }
}
