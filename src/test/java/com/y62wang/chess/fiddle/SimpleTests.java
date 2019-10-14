package com.y62wang.chess.fiddle;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.CharacterUtilities;
import com.y62wang.chess.BoardConstants;
import org.junit.Test;

public class SimpleTests {

    @Test
    public void testEndianConversion() {
        char[] chars = CharacterUtilities.toLittleEndianBoard(BoardConstants.NEW_BOARD_CHARS);
        System.out.println(chars);
        System.out.println(BoardConstants.NEW_BOARD_CHARS);
    }

    @Test
    public void testNewBoard()  {
        System.out.println(new Bitboard());
    }
}
