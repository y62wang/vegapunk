package com.y62wang.chess.engine.bits;

import com.google.common.base.Verify;

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
        Verify.verify(number != 0, "0 does not have LS1B");
        return Long.numberOfTrailingZeros(number);
    }
}
