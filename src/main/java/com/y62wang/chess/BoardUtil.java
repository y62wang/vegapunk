package com.y62wang.chess;

import com.y62wang.chess.bits.BitScan;

import java.util.ArrayList;
import java.util.List;

import static com.y62wang.chess.BoardConstants.*;

public class BoardUtil
{
    private static final long[][] SQUARES_BB;
    private static final long[] SQUARES_BB_SINGLE;

    static
    {
        SQUARES_BB = new long[BOARD_DIM][BOARD_DIM];
        for (int i = 0; i < BOARD_DIM; i++)
        {
            for (int j = 0; j < BOARD_DIM; j++)
            {
                SQUARES_BB[i][j] = 1L << square(i, j);
            }
        }

        SQUARES_BB_SINGLE = new long[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            SQUARES_BB_SINGLE[i] = 1L << i;
        }
    }

    public static int rank(int square)
    {
        return square / BOARD_DIM;
    }

    public static int file(int square)
    {
        return square % BOARD_DIM;
    }

    public static long fileBB(int square)
    {
        return FILE_A << file(square);
    }

    public static long rankBB(int square)
    {
        return RANK_1 << (rank(square) * BOARD_DIM);
    }

    public static long squareBB(int file, int rank)
    {
        return SQUARES_BB[file][rank];
    }

    public static long squareBB(int square)
    {
        return SQUARES_BB_SINGLE[square];
    }

    public static int square(int file, int rank)
    {
        return (rank * BOARD_DIM + file);
    }

    public static long position(int... squares)
    {
        long position = 0;
        for (int i = 0; i < squares.length; i++) position |= squareBB(squares[i]);
        return position;
    }

    public static boolean isValidSquare(int square)
    {
        return square >= 0 && square < 64;
    }

    public static boolean isWithinDimension(int fileOrRank)
    {
        return fileOrRank >= 0 && fileOrRank <= 7;
    }

    public static List<Integer> squaresOfBB(long bb)
    {
        List<Integer> list = new ArrayList<>();
        while (bb != 0)
        {
            int ls1b = BitScan.ls1b(bb);
            list.add(ls1b);
            bb = bb & ~(1L << ls1b);
        }
        return list;
    }
}
