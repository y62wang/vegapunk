package com.y62wang.chess.engine.search;

import com.y62wang.chess.engine.Bitboard;

public interface SearchAlgorithm
{
    SearchNode search(Bitboard board, int depth);

    SearchNode iterativeDeepening(Bitboard board, int depth, SearchResult intermediateResult);
}
