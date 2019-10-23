package com.y62wang.chess.enums;

public enum PieceType
{
    PAWN(0),
    KNIGHT(1),
    BISHOP(2),
    ROOK(3),
    QUEEN(4),
    KING(5),
    NO_TYPE(6);

    public static final int NUM_TYPES = 6;

    public final int index;

    PieceType(final int index)
    {
        this.index = index;
    }
}
