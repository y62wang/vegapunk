package com.y62wang.chess.engine.search;

import com.y62wang.chess.engine.Bitboard;
import com.y62wang.chess.engine.Move;
import com.y62wang.chess.engine.TranspositionTable;
import org.junit.Test;

public class AlphaBetaTest
{
    @Test
    public void testQuietPositionSearch() {
        AlphaBeta algorithm = new AlphaBeta(new TranspositionTable<>());
        Bitboard bb1 = new Bitboard("4k1qr/4p2n/8/6N1/8/8/4P2R/4K2R w KQkq -");
        double quiesce = algorithm.quiesce(bb1, AlphaBeta.NEGATIVE_INFINITY, AlphaBeta.POSITIVE_INFINITY);
        System.out.println(quiesce);
    }

    @Test
    public void testBlackSearch()
    {
        AlphaBeta algorithm = new AlphaBeta(new TranspositionTable<>());
        Bitboard bb1 = new Bitboard("rnb1kbnr/pppp1ppp/8/4p3/4P2q/5N2/PPPPQPPP/RNB1KB1R b KQkq -");
        SearchNode search = algorithm.search(bb1, 4);
        System.out.println(Move.moveString(search.move) + " " + search.score);
    }

    @Test
    public void testQuietPositionSearch2() {
        AlphaBeta algorithm = new AlphaBeta(new TranspositionTable<>());
        Bitboard bb1 = new Bitboard("rnb1kbnr/ppp1pppp/8/3p4/8/8/PP2PPPP/RNBQKBNR b KQkq - 0 1");
        System.out.println(bb1);
        SearchNode res= algorithm.iterativeDeepening(bb1, 8, new SearchResult((short)-1, -1));
        System.out.println(res.score);
    }
}
