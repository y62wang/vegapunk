package com.y62wang.chess.bits;

public class BitMask
{
    public static final long MASK_8_BITS = getMask(8);

    public static long getMask(int bits) {
        return (1 << bits) -1;
    }

    public static long getMask(int bits, int leftShift) {
        return getMask(bits) << leftShift;
    }
}
