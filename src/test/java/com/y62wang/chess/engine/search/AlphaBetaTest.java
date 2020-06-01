package com.y62wang.chess.engine.search;

import com.y62wang.chess.engine.Bitboard;
import com.y62wang.chess.engine.Move;
import com.y62wang.chess.engine.search.AlphaBeta.Node;
import org.junit.Test;

public class AlphaBetaTest
{
    @Test
    public void testQuietPositionSearch() {
        Bitboard bb1 = new Bitboard("4k1qr/4p2n/8/6N1/8/8/4P2R/4K2R w KQkq -");
        double quiesce = AlphaBeta.quiesce(bb1, AlphaBeta.NEGATIVE_INFINITY, AlphaBeta.POSITIVE_INFINITY);
        System.out.println(quiesce);
    }

    @Test
    public void testBlackSearch()
    {
        Bitboard bb1 = new Bitboard("rnb1kbnr/pppp1ppp/8/4p3/4P2q/5N2/PPPPQPPP/RNB1KB1R b KQkq -");
//        double quiesce = AlphaBeta.quiesce(bb1, AlphaBeta.NEGATIVE_INFINITY, AlphaBeta.POSITIVE_INFINITY);
//        System.out.println(quiesce);
        Node search = AlphaBeta.search(bb1, 4);
        System.out.println(Move.moveString(search.move) + " " + search.score);
    }
}
