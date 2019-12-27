package com.y62wang.chess.search;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Evaluation;
import com.y62wang.chess.Move;
import com.y62wang.chess.TranspositionTable;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class AlphaBeta
{

    public static final int NEGATIVE_INFINITY = -10000000;
    public static final int POSITIVE_INFINITY = 10000000;

    private static TranspositionTable<TranspositionEntry> tt = new TranspositionTable<>();

    private static class TranspositionEntry
    {
        short bestMove;
        double score;
        double depth;

        public TranspositionEntry(final short bestMove, final double score, final double depth)
        {
            this.bestMove = bestMove;
            this.score = score;
            this.depth = depth;
        }
    }

    public static class Node
    {
        public short move;
        public double score;

        public Node(final short move, final double score)
        {
            this.move = move;
            this.score = score;
        }
    }

    public static Node search(Bitboard board, int depth)
    {
        tt.cleanup();
        Node result = alphabeta(board, depth, NEGATIVE_INFINITY, POSITIVE_INFINITY, board.isWhiteTurn(), null);
        return result;
    }

    private static Node firstLevelSearchMax(Bitboard bb, int depth)
    {
        double maxScore = NEGATIVE_INFINITY;
        short best = 0;
        for (final short move : bb.legalMoves())
        {
            bb.makeMove(move);
            double result = -negamax2(bb, depth - 1, NEGATIVE_INFINITY, POSITIVE_INFINITY);
            if (result > maxScore)
            {
                maxScore = result;
                best = move;
            }
            bb.unmake();
        }
        return new Node(best, maxScore);
    }

    private static Node firstLevelSearchMin(Bitboard bb, int depth)
    {
        double minScore = NEGATIVE_INFINITY;
        short best = 0;
        for (final short move : bb.legalMoves())
        {
            bb.makeMove(move);
            double result = -negamax2(bb, depth - 1, NEGATIVE_INFINITY, POSITIVE_INFINITY);
            if (result > minScore)
            {
                minScore = result;
                best = move;
            }
            bb.unmake();
        }
        return new Node(best, minScore);
    }

    public static Node alphabeta(Bitboard board, int depth, double alpha, double beta, boolean maximizing, Node rootMove)
    {
        long key = tt.hash(board);

        if (depth == 0)
        {
            double score = quiesce(board, alpha, beta);
            if (!board.isWhiteTurn())
            {
                score = score * -1;
            }
            //tt.put(key, new TranspositionEntry(rootMove.move, score, depth));
            //return new Node(rootMove.move, Evaluation.evaluate(board));

            return new Node(rootMove.move, score);
        }

        TranspositionEntry value = tt.getValue(key);
        if (value != null && value.depth >= 2 && rootMove != null)
        {
//            System.out.println(board);
//            System.out.println(String.format("Move: %s E: %s A: %s\n", Move.moveString(rootMove.move), value.score, Evaluation.evaluate(board)));
            return new Node(rootMove.move, value.score);
        }

        short[] moves = board.legalMoves();

        if (maximizing)
        {
            if (moves.length == 0)
            {
                return rootMove == null ? new Node(( short ) 0, NEGATIVE_INFINITY) : new Node(rootMove.move, NEGATIVE_INFINITY);
            }

            Node maxEval = new Node(rootMove == null ? moves[0] : rootMove.move, NEGATIVE_INFINITY);

            for (final short move : moves)
            {
                board.makeMove(move);
                Node result = alphabeta(board, depth - 1, alpha, beta, false, rootMove == null ? new Node(move, NEGATIVE_INFINITY) : rootMove);
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
            tt.put(key, new TranspositionEntry(maxEval.move, maxEval.score, depth));
            return maxEval;
        }
        else
        {
            if (moves.length == 0)
            {
                return rootMove == null ? new Node(( short ) 0, POSITIVE_INFINITY) : new Node(rootMove.move, POSITIVE_INFINITY);
            }
            Node minEval = new Node(rootMove == null ? moves[0] : rootMove.move, POSITIVE_INFINITY);
            for (final short move : moves)
            {
                board.makeMove(move);
                Node result = alphabeta(board, depth - 1, alpha, beta, true, rootMove == null ? new Node(move, POSITIVE_INFINITY) : rootMove);
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
            tt.put(key, new TranspositionEntry(minEval.move, minEval.score, depth));
            return minEval;
        }
    }

    public static Node negamax(Bitboard board, int depth, double alpha, double beta, Node rootMove)
    {
        if (depth == 0)
        {
            return new Node(rootMove.move, quiesce(board, alpha, beta));
        }

        Node alphaNode = null;

        for (final short move : board.legalMoves())
        {
            if (rootMove == null)
            {
                rootMove = new Node(move, NEGATIVE_INFINITY);
            }

            board.makeMove(move);
            Node resultNode = negamax(board, depth - 1, -beta, -alpha, rootMove);
            double result = -resultNode.score;
            board.unmake();

            if (result >= beta)
            {
                return new Node(rootMove.move, beta);
            }

            if (result > alpha)
            {
                alpha = result;
                alphaNode = new Node(rootMove.move, result);
            }
        }
        return new Node(rootMove.move, alpha);

    }

    public static double negamax2(Bitboard board, int depth, double alpha, double beta)
    {
        if (depth == 0)
        {
            return quiesce(board, alpha, beta);
        }

        for (final short move : board.legalMoves())
        {
            board.makeMove(move);
            double result = -negamax2(board, depth - 1, -beta, -alpha);
            board.unmake();

            if (result >= beta)
            {
                return beta;
            }

            if (result > alpha)
            {
                alpha = result;
            }
        }
        return alpha;
    }

    public static double quiesce(Bitboard board, double alpha, double beta)
    {
        long key = tt.hash(board);

        TranspositionEntry value = tt.getValue(key);
        if (value != null && value.depth >= 2)
        {
            return value.score;
        }

        double val = Evaluation.evaluateAbsolute(board);

        if (val >= beta)
        {
            tt.put(key, new TranspositionEntry(( short ) 0, beta, 0));
            return beta;
        }

        if (alpha < val)
        {
            alpha = val;
        }

        for (final short move : board.legalMoves())
        {
            if (!Move.isCapture(move))
            {
                continue;
            }

            board.makeMove(move);
            double result = -quiesce(board, -beta, -alpha);
            board.unmake();

            if (result >= beta)
            {
                tt.put(key, new TranspositionEntry(( short ) 0, beta, 0));
                return beta;
            }

            if (result > alpha)
            {
                alpha = result;
            }
        }
        tt.put(key, new TranspositionEntry(( short ) 0, alpha, 0));
        return alpha;
    }

    public static Node minimax(Bitboard board, int depth, boolean maximizing, Node rootMove)
    {
        if (depth == 0)
        {
            return new Node(rootMove.move, Evaluation.evaluate(board));
        }

        if (maximizing)
        {
            Node maxEval = new Node(( short ) 0, NEGATIVE_INFINITY);
            short[] moves = board.legalMoves();
            for (final short move : moves)
            {
                board.makeMove(move);
                Node result = minimax(board, depth - 1, false, rootMove == null ? new Node(move, NEGATIVE_INFINITY) : rootMove);
                board.unmake();
                if (maxEval.score < result.score)
                {
                    maxEval.score = result.score;
                    maxEval.move = move;
                }
            }
            return maxEval;
        }
        else
        {
            Node minEval = new Node(( short ) 0, POSITIVE_INFINITY);
            short[] moves = board.legalMoves();
            for (final short move : moves)
            {
                board.makeMove(move);
                Node result = minimax(board, depth - 1, true, rootMove == null ? new Node(move, POSITIVE_INFINITY) : rootMove);
                board.unmake();
                if (result.score < minEval.score)
                {
                    minEval.score = result.score;
                    minEval.move = move;
                }
            }
            return minEval;
        }
    }

}
