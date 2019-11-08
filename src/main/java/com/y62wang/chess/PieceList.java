package com.y62wang.chess;

import com.y62wang.chess.bits.Endianess;
import com.y62wang.chess.enums.Piece;
import com.y62wang.chess.enums.PieceType;
import com.y62wang.chess.enums.Side;

import java.util.stream.IntStream;

import static com.y62wang.chess.Bitboard.BOARD_WIDTH;
import static com.y62wang.chess.Bitboard.SIZE;
import static com.y62wang.chess.enums.PieceType.NUM_TYPES;
import static com.y62wang.chess.enums.Side.NUM_SIDES;

public class PieceList
{
    public static final int MAX_PIECES = 10;

    private Piece[] board;
    private int[][][] pieces;
    private int[][] count;

    public PieceList()
    {
        pieces = new int[NUM_SIDES][NUM_TYPES][MAX_PIECES];
        count = new int[NUM_SIDES][NUM_TYPES];
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
    }

    private void removePiece(Side side, PieceType type, int square)
    {
        int[] list = pieces[side.index][type.index];
        int size = count[side.index][type.index];
        assert size > 0;

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
    }

    public Piece removePiece(int square)
    {
        Piece piece = board[square];
        if (piece != null)
        {
            removePiece(piece.side, piece.type, square);
        }
        assert onSquare(square) == Piece.NO_PIECE;
        return piece;
    }

    public void movePiece(int from, int to)
    {
        Piece piece = onSquare(from);
        assert piece != null && piece != Piece.NO_PIECE;
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
                for (int i = 0; i < count[side][type]; i++)
                {
                    occupied |= 1L << pieces[side][type][i];
                }
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
        long piecesBB = 0L;
        for (int i = 0; i < count[piece.side.index][piece.type.index]; i++)
        {
            piecesBB |= 1L << pieces[piece.side.index][piece.type.index][i];
        }
        return piecesBB;
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
                            Piece piece = this.onSquare(i);
                            if (piece != Piece.NO_PIECE)
                            {
                                sb.append(piece.pieceName());
                            }
                            else
                            {
                                sb.append('.');
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

    public void debug()
    {
    }
}
