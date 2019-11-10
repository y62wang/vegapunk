package com.y62wang.chess;

import static com.y62wang.chess.BoardUtil.position;

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

    public static final int SQ_A1 = 0;
    public static final int SQ_A2 = 9;
    public static final int SQ_A3 = 16;
    public static final int SQ_A4 = 24;
    public static final int SQ_A5 = 32;
    public static final int SQ_A6 = 40;
    public static final int SQ_A7 = 48;
    public static final int SQ_A8 = 56;

    public static final int SQ_B1 = SQ_A1 + 1;
    public static final int SQ_B2 = SQ_A2 + 1;
    public static final int SQ_B3 = SQ_A3 + 1;
    public static final int SQ_B4 = SQ_A4 + 1;
    public static final int SQ_B5 = SQ_A5 + 1;
    public static final int SQ_B6 = SQ_A6 + 1;
    public static final int SQ_B7 = SQ_A7 + 1;
    public static final int SQ_B8 = SQ_A8 + 1;

    public static final int SQ_C1 = SQ_B1 + 1;
    public static final int SQ_C2 = SQ_B2 + 1;
    public static final int SQ_C3 = SQ_B3 + 1;
    public static final int SQ_C4 = SQ_B4 + 1;
    public static final int SQ_C5 = SQ_B5 + 1;
    public static final int SQ_C6 = SQ_B6 + 1;
    public static final int SQ_C7 = SQ_B7 + 1;
    public static final int SQ_C8 = SQ_B8 + 1;

    public static final int SQ_D1 = SQ_C1 + 1;
    public static final int SQ_D2 = SQ_C2 + 1;
    public static final int SQ_D3 = SQ_C3 + 1;
    public static final int SQ_D4 = SQ_C4 + 1;
    public static final int SQ_D5 = SQ_C5 + 1;
    public static final int SQ_D6 = SQ_C6 + 1;
    public static final int SQ_D7 = SQ_C7 + 1;
    public static final int SQ_D8 = SQ_C8 + 1;

    public static final int SQ_E1 = SQ_D1 + 1;
    public static final int SQ_E2 = SQ_D2 + 1;
    public static final int SQ_E3 = SQ_D3 + 1;
    public static final int SQ_E4 = SQ_D4 + 1;
    public static final int SQ_E5 = SQ_D5 + 1;
    public static final int SQ_E6 = SQ_D6 + 1;
    public static final int SQ_E7 = SQ_D7 + 1;
    public static final int SQ_E8 = SQ_D8 + 1;

    public static final int SQ_F1 = SQ_E1 + 1;
    public static final int SQ_F2 = SQ_E2 + 1;
    public static final int SQ_F3 = SQ_E3 + 1;
    public static final int SQ_F4 = SQ_E4 + 1;
    public static final int SQ_F5 = SQ_E5 + 1;
    public static final int SQ_F6 = SQ_E6 + 1;
    public static final int SQ_F7 = SQ_E7 + 1;
    public static final int SQ_F8 = SQ_E8 + 1;

    public static final int SQ_G1 = SQ_F1 + 1;
    public static final int SQ_G2 = SQ_F2 + 1;
    public static final int SQ_G3 = SQ_F3 + 1;
    public static final int SQ_G4 = SQ_F4 + 1;
    public static final int SQ_G5 = SQ_F5 + 1;
    public static final int SQ_G6 = SQ_F6 + 1;
    public static final int SQ_G7 = SQ_F7 + 1;
    public static final int SQ_G8 = SQ_F8 + 1;

    public static final int SQ_H1 = SQ_G1 + 1;
    public static final int SQ_H2 = SQ_G2 + 1;
    public static final int SQ_H3 = SQ_G3 + 1;
    public static final int SQ_H4 = SQ_G4 + 1;
    public static final int SQ_H5 = SQ_G5 + 1;
    public static final int SQ_H6 = SQ_G6 + 1;
    public static final int SQ_H7 = SQ_G7 + 1;
    public static final int SQ_H8 = SQ_G8 + 1;

    public static final long BB_A1 = BoardUtil.squareBB(SQ_A1);
    public static final long BB_A2 = BoardUtil.squareBB(SQ_A2);
    public static final long BB_A3 = BoardUtil.squareBB(SQ_A3);
    public static final long BB_A4 = BoardUtil.squareBB(SQ_A4);
    public static final long BB_A5 = BoardUtil.squareBB(SQ_A5);
    public static final long BB_A6 = BoardUtil.squareBB(SQ_A6);
    public static final long BB_A7 = BoardUtil.squareBB(SQ_A7);
    public static final long BB_A8 = BoardUtil.squareBB(SQ_A8);
    public static final long BB_B1 = BoardUtil.squareBB(SQ_B1);
    public static final long BB_B2 = BoardUtil.squareBB(SQ_B2);
    public static final long BB_B3 = BoardUtil.squareBB(SQ_B3);
    public static final long BB_B4 = BoardUtil.squareBB(SQ_B4);
    public static final long BB_B5 = BoardUtil.squareBB(SQ_B5);
    public static final long BB_B6 = BoardUtil.squareBB(SQ_B6);
    public static final long BB_B7 = BoardUtil.squareBB(SQ_B7);
    public static final long BB_B8 = BoardUtil.squareBB(SQ_B8);
    public static final long BB_C1 = BoardUtil.squareBB(SQ_C1);
    public static final long BB_C2 = BoardUtil.squareBB(SQ_C2);
    public static final long BB_C3 = BoardUtil.squareBB(SQ_C3);
    public static final long BB_C4 = BoardUtil.squareBB(SQ_C4);
    public static final long BB_C5 = BoardUtil.squareBB(SQ_C5);
    public static final long BB_C6 = BoardUtil.squareBB(SQ_C6);
    public static final long BB_C7 = BoardUtil.squareBB(SQ_C7);
    public static final long BB_C8 = BoardUtil.squareBB(SQ_C8);
    public static final long BB_D1 = BoardUtil.squareBB(SQ_D1);
    public static final long BB_D2 = BoardUtil.squareBB(SQ_D2);
    public static final long BB_D3 = BoardUtil.squareBB(SQ_D3);
    public static final long BB_D4 = BoardUtil.squareBB(SQ_D4);
    public static final long BB_D5 = BoardUtil.squareBB(SQ_D5);
    public static final long BB_D6 = BoardUtil.squareBB(SQ_D6);
    public static final long BB_D7 = BoardUtil.squareBB(SQ_D7);
    public static final long BB_D8 = BoardUtil.squareBB(SQ_D8);
    public static final long BB_E1 = BoardUtil.squareBB(SQ_E1);
    public static final long BB_E2 = BoardUtil.squareBB(SQ_E2);
    public static final long BB_E3 = BoardUtil.squareBB(SQ_E3);
    public static final long BB_E4 = BoardUtil.squareBB(SQ_E4);
    public static final long BB_E5 = BoardUtil.squareBB(SQ_E5);
    public static final long BB_E6 = BoardUtil.squareBB(SQ_E6);
    public static final long BB_E7 = BoardUtil.squareBB(SQ_E7);
    public static final long BB_E8 = BoardUtil.squareBB(SQ_E8);
    public static final long BB_F1 = BoardUtil.squareBB(SQ_F1);
    public static final long BB_F2 = BoardUtil.squareBB(SQ_F2);
    public static final long BB_F3 = BoardUtil.squareBB(SQ_F3);
    public static final long BB_F4 = BoardUtil.squareBB(SQ_F4);
    public static final long BB_F5 = BoardUtil.squareBB(SQ_F5);
    public static final long BB_F6 = BoardUtil.squareBB(SQ_F6);
    public static final long BB_F7 = BoardUtil.squareBB(SQ_F7);
    public static final long BB_F8 = BoardUtil.squareBB(SQ_F8);
    public static final long BB_G1 = BoardUtil.squareBB(SQ_G1);
    public static final long BB_G2 = BoardUtil.squareBB(SQ_G2);
    public static final long BB_G3 = BoardUtil.squareBB(SQ_G3);
    public static final long BB_G4 = BoardUtil.squareBB(SQ_G4);
    public static final long BB_G5 = BoardUtil.squareBB(SQ_G5);
    public static final long BB_G6 = BoardUtil.squareBB(SQ_G6);
    public static final long BB_G7 = BoardUtil.squareBB(SQ_G7);
    public static final long BB_G8 = BoardUtil.squareBB(SQ_G8);
    public static final long BB_H1 = BoardUtil.squareBB(SQ_H1);
    public static final long BB_H2 = BoardUtil.squareBB(SQ_H2);
    public static final long BB_H3 = BoardUtil.squareBB(SQ_H3);
    public static final long BB_H4 = BoardUtil.squareBB(SQ_H4);
    public static final long BB_H5 = BoardUtil.squareBB(SQ_H5);
    public static final long BB_H6 = BoardUtil.squareBB(SQ_H6);
    public static final long BB_H7 = BoardUtil.squareBB(SQ_H7);
    public static final long BB_H8 = BoardUtil.squareBB(SQ_H8);

    public static final long OUTER_BOARD = BoardConstants.FILE_H | BoardConstants.FILE_A | BoardConstants.RANK_1 | BoardConstants.RANK_8;
    public static final long INNER_BOARD = ~(BoardConstants.FILE_H | BoardConstants.FILE_A | BoardConstants.RANK_1 | BoardConstants.RANK_8);

    public static final long W_KING_CASTLE_ATTACK_MASK = position(SQ_F1, SQ_G1);
    public static final long W_QUEEN_CASTLE_ATTACK_MASK = position(SQ_C1, SQ_D1);
    public static final long B_KING_CASTLE_ATTACK_MASK = position(SQ_F8, SQ_G8);
    public static final long B_QUEEN_CASTLE_ATTACK_MASK = position(SQ_C8, SQ_D8);

    public static final long W_KING_CASTLE_CLEAR_MASK = position(SQ_F1, SQ_G1);
    public static final long W_QUEEN_CASTLE_CLEAR_MASK = position(SQ_B1, SQ_C1, SQ_D1);
    public static final long B_KING_CASTLE_CLEAR_MASK = position(SQ_F8, SQ_G8);
    public static final long B_QUEEN_CASTLE_CLEAR_MASK = position(SQ_B8, SQ_C8, SQ_D8);
    public static final long[][] CASTLE_ATTACK_MASKS = new long[][] {
            {W_KING_CASTLE_ATTACK_MASK, W_QUEEN_CASTLE_ATTACK_MASK},
            {B_KING_CASTLE_ATTACK_MASK, B_QUEEN_CASTLE_ATTACK_MASK}};
    public static final long[][] CASTLE_CLEAR_MASKS = new long[][] {
            {W_KING_CASTLE_CLEAR_MASK, W_QUEEN_CASTLE_CLEAR_MASK},
            {B_KING_CASTLE_CLEAR_MASK, B_QUEEN_CASTLE_CLEAR_MASK}};
}
