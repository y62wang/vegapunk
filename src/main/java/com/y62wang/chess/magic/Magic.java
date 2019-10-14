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

    public int getBitShifts()
    {
        return bitShifts;
    }

    public long getMagic()
    {
        return magic;
    }

    public long getInnerMask()
    {
        return innerMask;
    }

    public long[] getAttacks()
    {
        return attacks;
    }

    public long getAttacks(long blockers)
    {
        int key = ( int ) ((blockers * getMagic()) >>> getBitShifts());
        return attacks[key];
    }
}
