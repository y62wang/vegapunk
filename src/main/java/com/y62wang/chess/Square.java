package com.y62wang.chess;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static com.y62wang.chess.BoardConstants.BOARD_SIZE;

public class Square
{
    private static final String[] RANK_STRINGS = new String[] {"1", "2", "3", "4", "5", "6", "7", "8"};
    private static final String[] FILE_STRINGS = new String[] {"A", "B", "C", "E", "E", "F", "G", "H"};
    private static final Map<Integer, String> SQUARE_STRINGS = new HashMap<>();

    static
    {
        IntStream.range(0, BOARD_SIZE).forEach(square -> SQUARE_STRINGS.put(square, mapToString(square)));
    }

    private static String mapToString(int square)
    {
        int file = BoardUtil.file(square);
        int rank = BoardUtil.rank(square);
        return FILE_STRINGS[file] + RANK_STRINGS[rank];
    }

    public static String squareString(int square)
    {
        return SQUARE_STRINGS.get(square);
    }

    public static String rankString(int rank) {
        return RANK_STRINGS[rank];
    }

    public static String fileString(int file) {
        return FILE_STRINGS[file];
    }
}
