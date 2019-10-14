package com.y62wang.chess;

import com.y62wang.chess.bits.Endianess;

import java.util.stream.IntStream;

import static com.y62wang.chess.BoardConstants.NEW_BOARD_CHARS;

public class Bitboard
{

    public static final int BOARD_WIDTH = 8;
    public static final int SIZE = 64;
    public static byte WHITE = 0;
    public static byte BLACK = 1;

    private long WP, WN, WB, WR, WK, WQ, BP, BN, BB, BR, BK, BQ;
    private boolean BKCastle, BQCastle, WKCastle, WQCastle;
    private int enPassantTarget;
    private byte turn;

    public Bitboard()
    {
        this(CharacterUtilities.toLittleEndianBoard(NEW_BOARD_CHARS));
    }

    public Bitboard(final char[] chars, final byte turn)
    {
        this(CharacterUtilities.toLittleEndianBoard(NEW_BOARD_CHARS));
        this.turn = turn;
    }

    public Bitboard(String FEN)
    {
        StringBuilder sb = new StringBuilder();
        String[] tokens = FEN.split(" ");
        if (tokens.length < 4)
        {
            throw new RuntimeException("Invalid FEN " + FEN);
        }
        for (int i = 0; i < tokens[0].length(); i++)
        {
            char c = tokens[0].charAt(i);
            if (Character.isDigit(c))
            {
                IntStream.range(0, Character.getNumericValue(c)).forEach(a -> sb.append(" "));
            }
            else if (c == '/')
            {
                continue;
            }
            else
            {
                sb.append(c);
            }
        }
        assignPiece(sb.toString().toCharArray());
        turn = ( byte ) (tokens[1].equalsIgnoreCase("w") ? 0 : 1);
        WKCastle = !tokens[2].contains("K");
        WQCastle = !tokens[2].contains("Q");
        BKCastle = !tokens[2].contains("k");
        BQCastle = !tokens[2].contains("q");
        enPassantTarget = tokens[4].equals("-") ? 0 : Integer.parseInt(tokens[4]);
    }

    public Bitboard(char[] board)
    {
        assignPiece(board);
    }

    public Bitboard(Bitboard bitboard)
    {
        this.WP = bitboard.WP;
        this.WN = bitboard.WN;
        this.WB = bitboard.WB;
        this.WR = bitboard.WR;
        this.WQ = bitboard.WQ;
        this.WK = bitboard.WK;
        this.BP = bitboard.BP;
        this.BB = bitboard.BB;
        this.BR = bitboard.BR;
        this.BN = bitboard.BN;
        this.BQ = bitboard.BQ;
        this.BK = bitboard.BK;
        this.WKCastle = bitboard.WKCastle;
        this.WQCastle = bitboard.WQCastle;
        this.BKCastle = bitboard.BKCastle;
        this.BQCastle = bitboard.BQCastle;
        this.turn = bitboard.turn;
        this.enPassantTarget = bitboard.enPassantTarget;
    }

    private void assignPiece(final char[] board)
    {
        if (board.length != SIZE)
        {
            throw new RuntimeException("invalid board size: " + board.length);
        }
        for (int i = 0; i < board.length; i++)
        {
            final char piece = board[i];
            final long pos = 1L << i;
            switch (piece)
            {
                case 'r':
                    BR = BR | pos;
                    break;
                case 'n':
                    BN = BN | pos;
                    break;
                case 'b':
                    BB = BB | pos;
                    break;
                case 'q':
                    BQ = BQ | pos;
                    break;
                case 'k':
                    BK = BK | pos;
                    break;
                case 'p':
                    BP = BP | pos;
                    break;
                case 'R':
                    WR = WR | pos;
                    break;
                case 'N':
                    WN = WN | pos;
                    break;
                case 'B':
                    WB = WB | pos;
                    break;
                case 'Q':
                    WQ = WQ | pos;
                    break;
                case 'K':
                    WK = WK | pos;
                    break;
                case 'P':
                    WP = WP | pos;
                    break;
                case ' ':
                    break;
                default:
                    throw new RuntimeException("Invalid piece " + piece);
            }
        }
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
                                sb.append(" ").append(8 - i / 8).append("   ");
                            }
                            if (1 == (1 & (BR >>> i)))
                            {
                                sb.append('r');
                            }
                            else if (1 == (1 & (BB >>> i)))
                            {
                                sb.append('b');
                            }
                            else if (1 == (1 & (BN >>> i)))
                            {
                                sb.append('n');
                            }
                            else if (1 == (1 & (BQ >>> i)))
                            {
                                sb.append('q');
                            }
                            else if (1 == (1 & (BK >>> i)))
                            {
                                sb.append('k');
                            }
                            else if (1 == (1 & (BP >>> i)))
                            {
                                sb.append('p');
                            }
                            else if (1 == (1 & (WR >>> i)))
                            {
                                sb.append('R');
                            }
                            else if (1 == (1 & (WB >>> i)))
                            {
                                sb.append('B');
                            }
                            else if (1 == (1 & (WN >>> i)))
                            {
                                sb.append('N');
                            }
                            else if (1 == (1 & (WQ >>> i)))
                            {
                                sb.append('Q');
                            }
                            else if (1 == (1 & (WK >>> i)))
                            {
                                sb.append('K');
                            }
                            else if (1 == (1 & (WP >>> i)))
                            {
                                sb.append('P');
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

    public boolean isWhiteTurn()
    {
        return turn == WHITE;
    }

    public long getOccupiedSquares()
    {
        return WP | WN | WB | WR | WK | WQ | BP | BN | BB | BR | BK | BQ;
    }

    public long getEmptySquares()
    {
        return ~getOccupiedSquares();
    }
}
