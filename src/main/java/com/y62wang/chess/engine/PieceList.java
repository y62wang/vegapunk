package com.y62wang.chess.engine;

import com.google.common.base.Verify;
import com.y62wang.chess.engine.bits.Endianess;
import com.y62wang.chess.engine.enums.Piece;
import com.y62wang.chess.engine.enums.PieceType;
import com.y62wang.chess.engine.enums.Side;
import com.y62wang.chess.engine.util.UnicodeChessPieceUtil;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.y62wang.chess.engine.Bitboard.BOARD_WIDTH;
import static com.y62wang.chess.engine.Bitboard.SIZE;

public class PieceList
{
    public static int MAX_PIECES = 10;

    private Piece[] board;
    private int[][][] pieces;
    private long[][] piecesBB;
    private int[][] count;

    public PieceList()
    {
        pieces = new int[Side.NUM_SIDES][PieceType.NUM_TYPES][MAX_PIECES];
        piecesBB = new long[Side.NUM_SIDES][PieceType.NUM_TYPES];
        count = new int[Side.NUM_SIDES][PieceType.NUM_TYPES];
        board = new Piece[SIZE];
    }

    public void addPiece(Side side, PieceType pieceType, int square)
    {
        addPiece(Piece.of(side, pieceType), square);
    }

    public void addPiece(Piece piece, int square)
    {
        int tail = count[piece.side.index][piece.type.index]++;
        pieces[piece.side.index][piece.type.index][tail] = square;
        board[square] = piece;
        piecesBB[piece.side.index][piece.type.index] |= BoardUtil.squareBB(square);
    }

    private void removePiece(Side side, PieceType type, int square)
    {
        int[] list = pieces[side.index][type.index];
        int size = count[side.index][type.index];
        Verify.verify(size > 0);

        for (int i = 0; i < size; i++)
        {
            if (square == list[i])
            {
                // copy last valid square to this position
                list[i] = list[size - 1];
                break;
            }
        }

        list[size] = -1;
        count[side.index][type.index]--;
        board[square] = null;
        piecesBB[side.index][type.index] = piecesBB[side.index][type.index] & ~BoardUtil.squareBB(square);
    }

    public Piece removePiece(int square)
    {
        Piece piece = board[square];
        if (piece != null)
        {
            removePiece(piece.side, piece.type, square);
        }
        Verify.verify(onSquare(square) == Piece.NO_PIECE);
        return piece;
    }

    public void movePiece(int from, int to)
    {
        Piece piece = onSquare(from);

        Verify.verify(piece != null && piece != Piece.NO_PIECE);

        addPiece(piece, to);
        removePiece(from);
    }

    public Piece onSquare(int square)
    {
        return board[square] == null ? Piece.NO_PIECE : board[square];
    }

    public long occupied()
    {
        long occupied = 0;
        for (int side = 0; side < pieces.length; side++)
        {
            for (int type = 0; type < pieces[side].length; type++)
            {
                occupied |= piecesBB[side][type];
            }
        }

        return occupied;
    }

    public long sideBB(Side side)
    {
        long sideBB = 0;
        for (int type = 0; type < pieces[side.index].length; type++)
        {
            for (int i = 0; i < count[side.index][type]; i++)
            {
                sideBB |= 1L << pieces[side.index][type][i];
            }
        }
        return sideBB;
    }

    public long piecesBB(Side side, PieceType type)
    {
        return piecesBB(Piece.of(side, type));
    }

    public long piecesBB(Piece piece)
    {
        return piecesBB[piece.side.index][piece.type.index];
    }

    public PieceList copy()
    {
        PieceList copy = new PieceList();
        for (int i = 0; i < board.length; i++)
        {
            if (board[i] != null)
            {
                copy.addPiece(board[i], i);
            }
        }
        return copy;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        IntStream.range(0, SIZE)
                .map(bigEndianIndex -> Endianess.toLittleEndian(bigEndianIndex))
                .forEach(
                        i ->
                        {
                            if (i % 8 == 0)
                            {
                                sb.append(" ").append(1 + i / 8).append("   ");
                            }
                            Piece piece = onSquare(i);
                            if (piece != Piece.NO_PIECE)
                            {
                                sb.append(UnicodeChessPieceUtil.toUnicode(piece.pieceName()));
                            }
                            else
                            {
                                sb.append((((i/8) %2 + (i % 8) %2) %2) == 0 ? "." : "_");
                            }
                            sb.append("  ");
                            if ((i + 1) % BOARD_WIDTH == 0)
                            {
                                sb.append("\n");
                            }
                        });
        sb.append("\n     A  B  C  D  E  F  G  H");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PieceList pieceList = ( PieceList ) o;
        return Arrays.equals(board, pieceList.board) &&
               Arrays.equals(pieces, pieceList.pieces) &&
               Arrays.equals(count, pieceList.count);
    }

    @Override
    public int hashCode()
    {
        int result = Arrays.hashCode(board);
        result = 31 * result + Arrays.hashCode(pieces);
        result = 31 * result + Arrays.hashCode(count);
        return result;
    }
}
