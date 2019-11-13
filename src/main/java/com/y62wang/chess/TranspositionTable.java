package com.y62wang.chess;

import com.y62wang.chess.enums.Piece;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TranspositionTable<T>
{
    private static final int WP = 1;
    private Random random;
    private long[][] piecesKeys;
    private long[] keys = new long[13];
    private Map<Long, T> tt;

    public <T> TranspositionTable()
    {
        piecesKeys = new long[Bitboard.SIZE][12];
        keys = new long[13];
        random = new Random(0);
        fillTable();
        tt = new HashMap<>();
    }

    private void fillTable()
    {
        for (int i = 0; i < piecesKeys.length; i++)
        {
            for (int j = 0; j < piecesKeys[0].length; j++)
            {
                piecesKeys[i][j] = random.nextLong();
            }
        }
        for (int i = 0; i < keys.length; i++)
        {
            keys[i] = random.nextLong();
        }
    }

    public long hash(Bitboard bb)
    {
        long result = 0;
        for (int i = 0; i < piecesKeys.length; i++)
        {
            Piece piece = bb.getPieceList().onSquare(i);
            if (piece == Piece.NO_PIECE)
            {
                continue;
            }

            result ^= piecesKeys[i][piece.side.index * 6 + piece.type.index];
        }
        result ^= bb.isWhiteTurn() ? keys[0] : 0;
        result ^= bb.canKingSideCastleWhite() ? keys[1] : 0;
        result ^= bb.canQueenSideCastleWhite() ? keys[2] : 0;
        result ^= bb.canKingSideCastleBlack() ? keys[3] : 0;
        result ^= bb.canQueenSideCastleBlack() ? keys[4] : 0;
        int epFile = bb.getEPFile();
        if (epFile > 0)
        {
            result ^= keys[4 + epFile];
        }
        return result;
    }

    public T getValue(long key)
    {
        return tt.get(key);
    }

    public void put(long key, T t)
    {
        tt.put(key, t);
    }

    public void cleanup()
    {
        if(this.tt.size()>10000000) {
            this.tt.clear();
        }
    }
}
