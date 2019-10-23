package com.y62wang.chess.enums;

public enum Side
{

    WHITE(0),
    BLACK(1),
    NO_SIDE(2);

    public static final int NUM_SIDES = 2;

    public final int index;

    Side(final int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return index;
    }

    public Side flip()
    {
        return this == WHITE ? BLACK : WHITE;
    }
}
