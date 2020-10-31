package com.y62wang.chess.engine.enums;

public enum Piece
{
    W_PAWN(Side.WHITE, PieceType.PAWN),
    W_KNIGHT(Side.WHITE, PieceType.KNIGHT),
    W_BISHOP(Side.WHITE, PieceType.BISHOP),
    W_ROOK(Side.WHITE, PieceType.ROOK),
    W_QUEEN(Side.WHITE, PieceType.QUEEN),
    W_KING(Side.WHITE, PieceType.KING),
    B_PAWN(Side.BLACK, PieceType.PAWN),
    B_KNIGHT(Side.BLACK, PieceType.KNIGHT),
    B_BISHOP(Side.BLACK, PieceType.BISHOP),
    B_ROOK(Side.BLACK, PieceType.ROOK),
    B_QUEEN(Side.BLACK, PieceType.QUEEN),
    B_KING(Side.BLACK, PieceType.KING),
    NO_PIECE(Side.NO_SIDE, PieceType.NO_TYPE);

    private static String PIECE_CHARS = "PNBRQKpnbrqk";
    private static Piece[] PIECES = new Piece[]
            {
                    W_PAWN, W_KNIGHT, W_BISHOP, W_ROOK, W_QUEEN, W_KING,
                    B_PAWN, B_KNIGHT, B_BISHOP, B_ROOK, B_QUEEN, B_KING,
                    NO_PIECE
            };

    private static Piece[][] PIECES_COLOR_TYPE_MAPPING = new Piece[][]
            {
                    {W_PAWN, W_KNIGHT, W_BISHOP, W_ROOK, W_QUEEN, W_KING},
                    {B_PAWN, B_KNIGHT, B_BISHOP, B_ROOK, B_QUEEN, B_KING}
            };

    public Side side;
    public PieceType type;

    Piece(Side side, PieceType type)
    {
        this.side = side;
        this.type = type;
    }

    public char pieceName()
    {
        return PIECE_CHARS.charAt(type.index + 6 * side.index);
    }

    public static Piece of(char pieceChar)
    {
        int index = PIECE_CHARS.indexOf(pieceChar);
        return index >= 0 ? PIECES[index] : NO_PIECE;
    }

    public static Piece of(Side side, PieceType type)
    {
        return PIECES_COLOR_TYPE_MAPPING[side.index][type.index];
    }
}
