package com.y62wang.chess;

public class BoardConstants
{
    public static final int BOARD_DIM = 8;
    public static final int BOARD_SIZE = BOARD_DIM * BOARD_DIM;

    public static final char[] NEW_BOARD_CHARS = new char[] {
            'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r',
            'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P',
            'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R',
    };

    public static final String NEW_GAME_FEN = "rnbqkbnr/pppppppp/BOARD_DIM/BOARD_DIM/BOARD_DIM/BOARD_DIM/PPPPPPPP/RNBQKBNR w KQkq -";

    public static final long BB_RANK_1 = (1 << BOARD_DIM) - 1;
    public static final long BB_RANK_2 = BB_RANK_1 << BOARD_DIM;
    public static final long BB_RANK_3 = BB_RANK_2 << BOARD_DIM;
    public static final long BB_RANK_4 = BB_RANK_3 << BOARD_DIM;
    public static final long BB_RANK_5 = BB_RANK_4 << BOARD_DIM;
    public static final long BB_RANK_6 = BB_RANK_5 << BOARD_DIM;
    public static final long BB_RANK_7 = BB_RANK_6 << BOARD_DIM;
    public static final long BB_RANK_8 = BB_RANK_7 << BOARD_DIM;

    public static final long BB_FILE_H = (1L << 63) | (1L << 55) | (1L << 47) | (1L << 39) | (1L << 31) | (1L << 23) | (1L << 15) | (1L << 7);
    public static final long BB_FILE_G = BB_FILE_H >>> 1;
    public static final long BB_FILE_F = BB_FILE_G >>> 1;
    public static final long BB_FILE_E = BB_FILE_F >>> 1;
    public static final long BB_FILE_D = BB_FILE_E >>> 1;
    public static final long BB_FILE_C = BB_FILE_D >>> 1;
    public static final long BB_FILE_B = BB_FILE_C >>> 1;
    public static final long BB_FILE_A = BB_FILE_B >>> 1;

    public static final int FILE_A = 0;
    public static final int FILE_B = 1;
    public static final int FILE_C = 2;
    public static final int FILE_D = 3;
    public static final int FILE_E = 4;
    public static final int FILE_F = 5;
    public static final int FILE_G = 6;
    public static final int FILE_H = 7;

    public static final int RANK_1 = 0;
    public static final int RANK_2 = 1;
    public static final int RANK_3 = 2;
    public static final int RANK_4 = 3;
    public static final int RANK_5 = 4;
    public static final int RANK_6 = 5;
    public static final int RANK_7 = 6;
    public static final int RANK_8 = 7;

    public static final long OUTER_BOARD = BoardConstants.FILE_H | BoardConstants.FILE_A | BoardConstants.RANK_1 | BoardConstants.RANK_8;
    /**
     * The constant INNER_BOARD.
     */
    public static final long INNER_BOARD = ~(BoardConstants.FILE_H | BoardConstants.FILE_A | BoardConstants.RANK_1 | BoardConstants.RANK_8);
}
