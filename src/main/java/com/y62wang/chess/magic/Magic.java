package com.y62wang.chess.magic;

public class Magic
{
    private int bitShifts;
    private long magic;
    private long innerMask;
    private long[] attacks;

    public Magic(final int bitShifts, final long magic, final long innerMask, final long[] attacks)
    {
        this.bitShifts = bitShifts;
        this.magic = magic;
        this.innerMask = innerMask;
        this.attacks = attacks;
    }

    public long getAttacks(long occupied)
    {
        long innerOccupied = occupied & innerMask;
        int key = ( int ) ((innerOccupied * magic) >>> bitShifts);
        return attacks[key];
    }
}
