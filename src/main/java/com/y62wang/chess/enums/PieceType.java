package com.y62wang.chess.enums;

public enum PieceType
{
    PAWN(0, 'p'),
    KNIGHT(1, 'n'),
    BISHOP(2, 'b'),
    ROOK(3, 'r'),
    QUEEN(4, 'q'),
    KING(5, 'k'),
    NO_TYPE(6, ' ');

    private static final PieceType[] map = new PieceType[] {PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING, NO_TYPE};
    private static final char[] pieceChars = new char[] {'p', 'n', 'b', 'r', 'q', 'k',' '};

    public static final int NUM_TYPES = 6;

    public final int index;
    public final char pieceChar;

    PieceType(final int index, final char c)
    {
        this.index = index;
        this.pieceChar = c;
    }

    public static PieceType of(int index)
    {
        return map[index];
    }

}
