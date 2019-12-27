package com.y62wang.chess.ui;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class UnicodeChessPieceUtil
{
    private static final Map<Character, String> pieceToUnicodeMap = ImmutableMap.<Character, String>builder()
            .put('K', "♔")
            .put('Q', "♕")
            .put('R', "♖")
            .put('B', "♗")
            .put('N', "♘")
            .put('P', "♙")
            .put('k', "♚")
            .put('q', "♛")
            .put('r', "♜")
            .put('b', "♝")
            .put('n', "♞")
            .put('p', "♟")
            .build();

    public static String toUnicode(char character)
    {
        return pieceToUnicodeMap.get(character);
    }
}
