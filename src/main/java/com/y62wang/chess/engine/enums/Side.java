package com.y62wang.chess.engine.enums;

public enum Side
{

    WHITE(0),
    BLACK(1),
    NO_SIDE(2);

    public static int NUM_SIDES = 2;

    public int index;

    Side(int index)
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
