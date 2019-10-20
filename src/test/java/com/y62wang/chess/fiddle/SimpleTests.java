package com.y62wang.chess.fiddle;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.BoardUtil;
import com.y62wang.chess.CharacterUtilities;
import com.y62wang.chess.BoardConstants;
import com.y62wang.chess.Move;
import com.y62wang.chess.Util;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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
        char[] boardArray = new char[] {
                'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r',
                'p', ' ', 'p', 'p', 'q', 'p', 'b', ' ',
                'b', ' ', ' ', ' ', 'p', 'n', 'p', ' ',
                ' ', ' ', ' ', ' ', 'N', ' ', ' ', ' ',
                ' ', 'p', ' ', ' ', 'P', ' ', ' ', ' ',
                ' ', ' ', 'N', ' ', ' ', 'Q', ' ', 'b',
                ' ', 'P', 'P', 'P', ' ', 'P', 'P', 'P',
                'R', ' ', 'r', 'B', 'K', ' ', ' ', 'R',
        };

        boardArray = new String(
"r       " +
" b k    " +
"   p    " +
"prp Kp  " +
"  P  P  " +
" p   q  " +
" P  N   " +
"        "
        ).toCharArray();

        Bitboard board = new Bitboard(boardArray, Bitboard.WHITE);
        System.out.println(board);
        board.debug();
    }

    @Test
    public void testPlay()
    {
        char[] boardArray = new char[] {
                'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r',
                'p', ' ', 'p', 'p', 'q', 'p', 'b', ' ',
                'b', ' ', ' ', ' ', 'p', 'n', 'p', ' ',
                ' ', ' ', ' ', ' ', 'N', ' ', ' ', ' ',
                ' ', 'p', ' ', ' ', 'P', ' ', ' ', ' ',
                ' ', ' ', 'N', ' ', ' ', 'Q', ' ', 'b',
                ' ', 'P', 'P', 'P', ' ', 'P', 'P', 'P',
                'R', ' ', 'r', 'B', 'K', ' ', ' ', 'R',
        };

        Bitboard board = new Bitboard(boardArray, Bitboard.WHITE);
        System.out.println(board);
        Random random = new Random(1L);
        for (int i = 0; i < 500; i++)
        {
            System.out.println("Move "+ i);
            List<Short> whiteLegal = board.whiteLegalMoves().stream().collect(Collectors.toList());
            if (whiteLegal.size() == 0)
            {
                System.out.println("WHITE LOST");
                break;
            }

            Short wMove = whiteLegal.get(random.nextInt(whiteLegal.size()));
            board = board.makeMove(wMove);
            System.out.println(Move.moveString(wMove) + " " + wMove);
            System.out.println(board.toString());

            List<Short> blackLegal = board.blackLegalMoves().stream().collect(Collectors.toList());

            if (blackLegal.size() == 0)
            {
                System.out.println("BLACK LOST");
                break;
            }
            Short bMove = blackLegal.get(random.nextInt(blackLegal.size()));
            board = board.makeMove(bMove);
            System.out.println(Move.moveString(bMove) + " " + bMove);
            System.out.println(board.toString());
        }
    }
}
