package com.y62wang.chess.engine.enums;

public enum PieceType
{
    PAWN(0, 'p'),
    KNIGHT(1, 'n'),
    BISHOP(2, 'b'),
    ROOK(3, 'r'),
    QUEEN(4, 'q'),
    KING(5, 'k'),
    NO_TYPE(6, ' ');

    private static PieceType[] map = new PieceType[] {PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING, NO_TYPE};
    private static char[] pieceChars = new char[] {'p', 'n', 'b', 'r', 'q', 'k',' '};

    public static int NUM_TYPES = 6;

    public int index;
    public char pieceChar;

    PieceType(int index, char c)
    {
        this.index = index;
        pieceChar = c;
    }

    public static PieceType of(int index)
    {
        return map[index];
    }

}
