package com.y62wang.chess.engine.bits;

public class BitMask
{
    public static long MASK_8_BITS = getMask(8);

    /**
     * get an n-bits mask at least significant bits
     *
     * @param n number of bits for the mask
     * @return
     */
    public static long getMask(int n)
    {
        return (1 << n) - 1;
    }

    /**
     * get an n-bits mask with left shift
     * @param n
     * @param leftShift
     * @return n-bits with left shift
     */
    public static long getMask(int n, int leftShift)
    {
        return getMask(n) << leftShift;
    }
}
