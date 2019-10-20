package com.y62wang.chess.fiddle;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.BoardUtil;
import com.y62wang.chess.CharacterUtilities;
import com.y62wang.chess.BoardConstants;
import com.y62wang.chess.Move;
import com.y62wang.chess.Util;
import org.junit.Test;

import static com.y62wang.chess.BoardConstants.BB_FILE_A;

public class SimpleTests
{

    @Test
    public void testEndianConversion()
    {
        char[] chars = CharacterUtilities.toLittleEndianBoard(BoardConstants.NEW_BOARD_CHARS);
        System.out.println(chars);
        System.out.println(BoardConstants.NEW_BOARD_CHARS);
    }

    @Test
    public void testNewBoard()
    {
        char[] boardArray = new char[]{
                'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r',
                'p', ' ', 'p', 'p', 'q', 'p', 'b', ' ',
                'b', ' ', ' ', ' ', 'p', 'n', 'p', ' ',
                ' ', ' ', ' ', ' ', 'N', ' ', ' ', ' ',
                ' ', 'p', ' ', ' ', 'P', ' ', ' ', ' ',
                ' ', ' ', 'N', ' ', ' ', 'Q', ' ', 'b',
                ' ', 'P', 'P', 'P', ' ', 'P', 'P', 'P',
                'R', ' ', 'r', 'B', 'K', 'q', ' ', 'R',
        };

        Bitboard board = new Bitboard(boardArray, Bitboard.WHITE);
        System.out.println(board);
        board.debug();
    }
}
