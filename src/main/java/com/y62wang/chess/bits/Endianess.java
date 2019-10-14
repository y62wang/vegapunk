package com.y62wang.chess.bits;

import static com.y62wang.chess.BoardConstants.BOARD_DIM;

public class Endianess
{

    public static int toLittleEndian(int number)
    {
        return (7 - (number / BOARD_DIM)) * BOARD_DIM + number % BOARD_DIM;
    }
}
