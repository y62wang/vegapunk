package com.y62wang.chess.bits;

public class BitScan
{
    /**
     * Find the index of the least significant 1 bit (LS1B)
     *
     * @param number
     * @return index of the LS1B
     */
    public static int scanForward(long number)
    {
        assert (number != 0);
        int index = 0;

        while (number != 0)
        {
            if ((number & (1L << index)) != 0)
            {
                return index;
            }
            index++;
        }
        return -1;
    }
}
