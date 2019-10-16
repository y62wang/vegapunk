package com.y62wang.chess;

import com.y62wang.chess.bits.BitMask;

import static com.y62wang.chess.BoardConstants.BOARD_SIZE;

public class Move
{

    private static final int SQUARE_MASK = ( int ) BitMask.getMask(6);
    private static final int MOVE_TYPE_MASK = ( int ) BitMask.getMask(4);

    public static final short QUIET_MOVE = 0;
    public static final short DOUBLE_PAWN_PUSH = 1;
    public static final short KING_CASTLE = 2;
    public static final short QUEEN_CASTLE = 3;
    public static final short CAPTURES = 4;
    public static final short EP_CAPTURE = 5;
    public static final short KNIGHT_PROMOTION = 8;
    public static final short BISHOP_PROMOTION = 9;
    public static final short ROOK_PROMOTION = 10;
    public static final short QUEEN_PROMOTION = 11;
    public static final short KNIGHT_PROMO_CAPTURE = 12;
    public static final short BISHOP_PROMO_CAPTURE = 13;
    public static final short ROOK_PROMO_CAPTURE = 14;
    public static final short QUEEN_PROMO_CAPTURE = 15;

    public static short move(int from, int to, int code)
    {
        assert from >= 0 && from < BOARD_SIZE;
        assert to >= 0 && to < BOARD_SIZE;
        assert code >= 0 && code < 16;

        short move = 0;
        move |= (SQUARE_MASK & from) << 10;
        move |= (SQUARE_MASK & to) << 4;
        move |= (MOVE_TYPE_MASK & code);
        return move;
    }

    public static boolean isPromotion(short move)
    {
        // 3rd bit is a promotion
        return (move & (1 << 3)) != 0;
    }

    public static boolean isCapture(short move)
    {
        return (move & (1 << 2)) != 0;
    }

    public static boolean isSpecial1(short move)
    {
        return (move & (1 << 1)) != 0;
    }

    public static boolean isSpecial0(short move)
    {
        return (move & 1) != 0;
    }

    public static boolean isCastle(short move)
    {
        return move == KING_CASTLE || move == QUEEN_CASTLE;
    }

    public static boolean isEnpassant(short move)
    {
        return move == EP_CAPTURE;
    }

    public static boolean isDoublePawnPush(short move)
    {
        return move == DOUBLE_PAWN_PUSH;
    }

    public static boolean isPromoCapture(short move)
    {
        return isPromotion(move) && isCapture(move);
    }

    public static int fromSquare(short move)
    {
        // bit shift will return an int, with padded 1's if the number is negative
        // therefore masking is required
        return (move >>> 10) & SQUARE_MASK;
    }

    public static int toSquare(short move)
    {
        return (move >>> 4) & SQUARE_MASK;
    }

    public static int moveCode(short move)
    {
        return move & MOVE_TYPE_MASK;
    }

    public static String moveString(short move)
    {
        if(moveCode(move) == KING_CASTLE) {
            return "O-O";
        }
        else if (moveCode(move) == QUEEN_CASTLE)
        {
            return "O-O-O";
        }
        return Square.squareString(fromSquare(move)) + Square.squareString(toSquare(move));
    }
}
