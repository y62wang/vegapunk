package com.y62wang.chess.engine.bits;

import static com.y62wang.chess.engine.BoardConstants.BOARD_DIM;

public class Endianess
{

    public static int toLittleEndian(int number)
    {
        return (7 - (number / BOARD_DIM)) * BOARD_DIM + number % BOARD_DIM;
    }
}
