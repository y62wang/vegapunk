package com.y62wang.chess.magic;

import com.y62wang.chess.BoardUtil;
import com.y62wang.chess.Rook;

import java.util.List;

import static com.y62wang.chess.BoardConstants.BOARD_SIZE;

public class MagicCache
{
    private final Magic[] bishopMagic;
    private final Magic[] rookMagic;

    public MagicCache()
    {
        bishopMagic = new Magic[BOARD_SIZE];
        rookMagic = new Magic[BOARD_SIZE];
        prepareRookMagic();
    }

    private void prepareRookMagic()
    {
        for (int square = 0; square < BOARD_SIZE; square++)
        {
            int bitShifts = BOARD_SIZE - MagicConstants.ROOK_MAGIC_BITS[square];
            long magicNumber = MagicConstants.ROOK_MAGIC[square];
            long mask = Rook.rookMask(square);
            long[] attacks = indexRookAttacksCache(square, bitShifts, magicNumber, mask);
            Magic magic = new Magic(bitShifts, magicNumber, mask, attacks);
            this.rookMagic[square] = magic;
        }
    }

    private long[] indexRookAttacksCache(final int square, final int bitShifts, final long magicNumber, final long mask)
    {
        List<Integer> squares = BoardUtil.squaresOfBB(mask);
        long[] attacks = new long[( int ) Math.pow(2, MagicConstants.ROOK_MAGIC_BITS[square])];
        for (int j = 0; j < ( int ) Math.pow(2, squares.size()); j++)
        {
            long occupancy = getOccupiedSquares(squares, j);
            long attackSet = Rook.rookAttacks(square, occupancy);
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
     * @param occupancyBits bit at position N indicates the square occupiedSquares.get(N) should bet set
     *
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
}
