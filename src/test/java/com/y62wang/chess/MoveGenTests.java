package com.y62wang.chess;

import com.y62wang.chess.perft.Perft;
import org.junit.Test;

import static com.y62wang.chess.BoardConstants.SQ_A1;
import static com.y62wang.chess.BoardConstants.SQ_A8;
import static com.y62wang.chess.BoardConstants.SQ_B1;
import static com.y62wang.chess.BoardConstants.SQ_B5;
import static com.y62wang.chess.BoardConstants.SQ_B8;
import static com.y62wang.chess.BoardConstants.SQ_C3;
import static com.y62wang.chess.BoardConstants.SQ_D5;
import static com.y62wang.chess.BoardConstants.SQ_D8;
import static com.y62wang.chess.BoardConstants.SQ_E2;
import static com.y62wang.chess.BoardConstants.SQ_E6;
import static com.y62wang.chess.BoardConstants.SQ_E8;

public class MoveGenTests
{

    @Test
    public void testCastle1()
    {
        Bitboard board = new Bitboard("4k3/4p3/8/8/8/8/P3P2P/R3K2R w KQkq -");
        Perft.singlePerft(board, 1, 17);
        Perft.singlePerft(board, 2, 94);
        Perft.singlePerft(board, 3, 1881);

        board = new Bitboard("r3k2r/p3p2p/8/8/8/8/P3P2P/R4RK1 b KQkq -");
        Perft.singlePerft(board, 1, 14);

        board = new Bitboard("r3k2r/p3p2p/8/8/8/8/P3P2P/R3K3 w KQkq -");
        Perft.singlePerft(board, 1, 14);

        board = new Bitboard("r3k2r/p3p2p/8/8/8/8/P3P2P/R3K2R w KQkq -");
        board = board.makeMove(Move.move(4, 6, Move.KING_CASTLE));
        System.out.println(board);
        Perft.singlePerft(board, 2, 275);
    }

    @Test
    public void testTT()
    {
        Bitboard board = new Bitboard("r3k2r/p1ppqpb1/Bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPB1PPP/R3K2R b KQkq -");

        short move = Move.move(SQ_C3, SQ_B1, Move.QUIET_MOVE);
        System.out.println(move);
        board = board.makeMove(move);
        board.debug();
        board = board.makeMove(Move.move(SQ_E6, SQ_D5, Move.QUIET_MOVE));
        board.debug();
        Util.printMoves2(board.legalMoves());
        // Perft.singlePerft(board, 1, 53);
        // Perft.singlePerft(board, 2, 1907);
    }
}
