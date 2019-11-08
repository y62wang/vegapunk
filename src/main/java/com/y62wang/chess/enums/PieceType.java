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

    private static final PieceType[] map = new PieceType[] {PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING, NO_TYPE};

    public static final int NUM_TYPES = 6;

    public final int index;

    PieceType(final int index)
    {
        this.index = index;
    }

    public static PieceType of(int index)
    {
        return map[index];
    }
}
