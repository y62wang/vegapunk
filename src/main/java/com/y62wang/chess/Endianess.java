package com.y62wang.chess;

public class Endianess
{

    public static int toLittleEndian(int number)
    {
        return (7 - (number / 8)) * 8 + number % 8;
    }
}
