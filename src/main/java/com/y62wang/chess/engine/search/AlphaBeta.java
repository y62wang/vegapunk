package com.y62wang.chess.engine.search;

import com.y62wang.chess.engine.Bitboard;
import com.y62wang.chess.engine.Evaluation;
import com.y62wang.chess.engine.Move;
import com.y62wang.chess.engine.TranspositionTable;
import lombok.extern.log4j.Log4j2;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Log4j2
public class AlphaBeta implements SearchAlgorithm
{

    public static final int NEGATIVE_INFINITY = -10000000;
    public static final int POSITIVE_INFINITY = 10000000;

    private final TranspositionTable<TranspositionEntry> transpositionTable;
    private final MoveSorter moveSorter;

    public AlphaBeta(TranspositionTable<TranspositionEntry> transpositionTable)
    {
        this.moveSorter = new MoveSorter();
        this.transpositionTable = transpositionTable;
    }

    @Override
    public SearchNode search(final Bitboard board, final int depth)
    {
        transpositionTable.cleanup();
        return alphabeta(board, depth, NEGATIVE_INFINITY, POSITIVE_INFINITY, board.isWhiteTurn(), null);
    }

    @Override
    public SearchNode iterativeDeepening(final Bitboard board, final int depth, final SearchResult intermediateResult)
    {
        transpositionTable.cleanup();
        SearchNode result = null;
        String before = board.toString();
        for (int i = 1; i <= depth; i++)
        {
            result = alphabeta(board, i, NEGATIVE_INFINITY, POSITIVE_INFINITY, board.isWhiteTurn(), null);

            if(!before.equals(board.toString())) {
                log.error("board state is wrong");
                log.error("before " + before);
                log.error("after" + board.toString());
            }
            log.info("ID: " + board.toString());
            intermediateResult.setMove(result.move);
            intermediateResult.setScore(result.score);
        }
        return result;
    }

    public SearchNode alphabeta(Bitboard board, int depth, double alpha, double beta, boolean maximizing, SearchNode rootMove)
    {
        long key = transpositionTable.hash(board);

        if (depth == 0)
        {
            double score = quiesce(board, alpha, beta);
            if (!board.isWhiteTurn())
            {
                score = score * -1;
            }

            return new SearchNode(rootMove.move, score);
        }

        TranspositionEntry value = transpositionTable.getValue(key);
        if (value != null && value.depth >= 2 && rootMove != null)
        {
            return new SearchNode(rootMove.move, value.score);
        }

        short[] moves = board.legalMoves();
        moveSorter.sort(moves);

        if (maximizing)
        {
            if (moves.length == 0)
            {
                return rootMove == null ? new SearchNode(( short ) 0, NEGATIVE_INFINITY) : new SearchNode(rootMove.move, NEGATIVE_INFINITY);
            }

            SearchNode maxEval = new SearchNode(rootMove == null ? moves[0] : rootMove.move, NEGATIVE_INFINITY);

            for (final short move : moves)
            {
                board.makeMove(move);
                SearchNode result = alphabeta(board, depth - 1, alpha, beta, false, rootMove == null ? new SearchNode(move, NEGATIVE_INFINITY) : rootMove);
                board.unmake();
                if (result.score > maxEval.score)
                {
                    maxEval.score = result.score;
                    maxEval.move = result.move;
                }
                alpha = max(alpha, result.score);
                if (beta <= alpha)
                {
                    break;
                }
            }
            transpositionTable.put(key, new TranspositionEntry(maxEval.move, maxEval.score, depth));
            return maxEval;
        }
        else
        {
            if (moves.length == 0)
            {
                return rootMove == null ? new SearchNode(( short ) 0, POSITIVE_INFINITY) : new SearchNode(rootMove.move, POSITIVE_INFINITY);
            }
            SearchNode minEval = new SearchNode(rootMove == null ? moves[0] : rootMove.move, POSITIVE_INFINITY);
            for (final short move : moves)
            {
                board.makeMove(move);
                SearchNode result = alphabeta(board, depth - 1, alpha, beta, true, rootMove == null ? new SearchNode(move, POSITIVE_INFINITY) : rootMove);
                board.unmake();
                if (result.score < minEval.score)
                {
                    minEval.score = result.score;
                    minEval.move = result.move;
                }
                beta = min(beta, result.score);
                if (beta <= alpha)
                {
                    break;
                }
            }
            transpositionTable.put(key, new TranspositionEntry(minEval.move, minEval.score, depth));
            return minEval;
        }
    }

    public double quiesce(Bitboard board, double alpha, double beta)
    {
        long key = transpositionTable.hash(board);

        TranspositionEntry value = transpositionTable.getValue(key);
        if (value != null && value.depth >= 2)
        {
            return value.score;
        }

        double val = Evaluation.evaluateAbsolute(board);

        if (val >= beta)
        {
            transpositionTable.put(key, new TranspositionEntry(( short ) 0, beta, 0));
            return beta;
        }

        if (alpha < val)
        {
            alpha = val;
        }

        short[] legalMoves = board.legalMoves();
        for (final short move : legalMoves)
        {
            if (!Move.isCapture(move))
            {
                continue;
            }

            String before = board.toString();
            board.makeMove(move);
            String after = board.toString();
            double result = -quiesce(board, -beta, -alpha);
            board.unmake();

            if (result >= beta)
            {
                transpositionTable.put(key, new TranspositionEntry(( short ) 0, beta, 0));
                return beta;
            }

            if (result > alpha)
            {
                alpha = result;
            }
        }
        transpositionTable.put(key, new TranspositionEntry(( short ) 0, alpha, 0));
        return alpha;
    }

}
