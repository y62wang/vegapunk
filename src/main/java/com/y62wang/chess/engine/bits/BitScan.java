package com.y62wang.chess.engine.bits;

public class BitScan
{
    /**
     * Find the index of the least significant 1 bit (LS1B)
     *
     * @param number
     * @return index of the LS1B
     */
    public static int ls1b(long number)
    {
        assert (number != 0);
        return Long.numberOfTrailingZeros(number);
    }
}
