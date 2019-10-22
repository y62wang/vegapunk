package com.y62wang.chess.bits;

public class PopulationCount
{
    private static final int NUM_CACHED_BITS = 8;
    private static final int CACHE_SIZE = ( int ) Math.pow(2, 8);
    private static final long MASK_8 = BitMask.getMask(8);
    private static final long MASK_16 = BitMask.getMask(16);

    private static final int[] popLookupTable8Bits = new int[CACHE_SIZE];
    private static final int[] popLookupTable16Bits = new int[CACHE_SIZE*CACHE_SIZE];

    static
    {
        for (int i = 0; i < popLookupTable8Bits.length; i++)
        {
            popLookupTable8Bits[i] = popCountWithoutLookup(i);
        }

        for (int i = 0; i < popLookupTable16Bits.length; i++)
        {
            popLookupTable16Bits[i] = popCount(i);
        }
    }

    private static int popCountWithoutLookup(long number)
    {
        int count = 0;
        while (number != 0)
        {
            // unset the least significant bit
            number = number & (number - 1);
            count++;
        }
        return count;
    }

    /**
     * This iteration based implementation is a bit less efficient than the non-iteration based version.
     *
     * @param number
     * @return population count
     */
    @Deprecated
    public static int popCountWithLoop(long number)
    {
        int count = 0;
        for (int i = 0; i < 64 / NUM_CACHED_BITS; i++)
        {
            int masked = ( int ) (MASK_8 & number);
            count += popLookupTable8Bits[masked];
            number = number >> NUM_CACHED_BITS;
        }
        return count;
    }

    public static int popCount(long number)
    {
        return Long.bitCount(number);
    }

    public static int popCount8(long number)
    {
        return popLookupTable8Bits[( int ) (MASK_8 & number)]
               + popLookupTable8Bits[( int ) (MASK_8 & (number) >> 8)]
               + popLookupTable8Bits[( int ) (MASK_8 & (number) >> 16)]
               + popLookupTable8Bits[( int ) (MASK_8 & (number) >> 24)]
               + popLookupTable8Bits[( int ) (MASK_8 & (number) >> 32)]
               + popLookupTable8Bits[( int ) (MASK_8 & (number) >> 40)]
               + popLookupTable8Bits[( int ) (MASK_8 & (number) >> 48)]
               + popLookupTable8Bits[( int ) (MASK_8 & (number) >> 56)];
    }

    public static int popCount16(long number)
    {
        return popLookupTable16Bits[( int ) (MASK_16 & number)]
               + popLookupTable16Bits[( int ) (MASK_16 & (number) >> 16)]
               + popLookupTable16Bits[( int ) (MASK_16 & (number) >> 32)]
               + popLookupTable16Bits[( int ) (MASK_16 & (number) >> 48)];

    }
}
