package com.y62wang.chess;

import org.junit.Test;

import java.util.Scanner;

public class MoveGenTests
{

    @Test
    public void testEnpassant() {
        Bitboard board = new Bitboard("rnbqkbnr/1ppppppp/8/1Pp5/8/P7/R1PPPPPP/1NBQKBNR w Kkq 3");
        System.out.println(board);
        Util.printMoves2(board.legalMoves());
        Bitboard bb = board.makeMove((short) -31067);
        System.out.println(Move.isEnpassant(( short ) -31067));
        System.out.println(bb);
    }
}
