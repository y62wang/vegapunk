package com.y62wang.chess;

import com.y62wang.chess.bits.Endianess;

public class CharacterUtilities
{
    public static char[] toLittleEndianBoard(char[] charBoard)
    {
        char[] result = new char[charBoard.length];
        for (int i = 0; i < charBoard.length; i++)
        {
            int targetIndex = Endianess.toLittleEndian(i);
            result[targetIndex] = charBoard[i];
        }
        return result;
    }
}
