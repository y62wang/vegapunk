package com.y62wang.chess;

import com.y62wang.chess.bits.BitScan;
import com.y62wang.chess.bits.Endianess;
import com.y62wang.chess.magic.MagicCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.y62wang.chess.BoardConstants.BB_FILE_A;
import static com.y62wang.chess.BoardConstants.BB_FILE_H;
import static com.y62wang.chess.BoardConstants.BB_RANK_1;
import static com.y62wang.chess.BoardConstants.BB_RANK_4;
import static com.y62wang.chess.BoardConstants.BB_RANK_5;
import static com.y62wang.chess.BoardConstants.BB_RANK_8;
import static com.y62wang.chess.BoardConstants.BOARD_DIM;
import static com.y62wang.chess.BoardConstants.NEW_BOARD_CHARS;
import static com.y62wang.chess.BoardConstants.RANK_8;

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
        this(CharacterUtilities.toLittleEndianBoard(chars));
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

    private long occupied()
    {
        return WP | WN | WB | WR | WK | WQ | BP | BN | BB | BR | BK | BQ;
    }

    private long emptySquares()
    {
        return ~occupied();
    }

    private long whitePieces()
    {
        return WP | WN | WB | WR | WK | WQ;
    }

    private long blackPieces()
    {
        return BP | BN | BB | BR | BK | BQ;
    }

    public long targets()
    {
        long occupied = occupied();
        long targets = kingTargets(WK)
                       | queenTargets(WQ, occupied)
                       | knightTargets(WN)
                       | rookTargets(WR, occupied)
                       | bishopTargets(WB, occupied)
                       | whitePawnsEastAttackTargets(WP)
                       | whitePawnsWestAttackTargets(WP);
        Util.printBitboard(targets);
        return targets;
    }

    private long knightTargets(long knights)
    {
        long targets = 0;
        while (knights != 0)
        {
            int ls1b = BitScan.ls1b(knights);
            knights &= ~shift(1L, ls1b);
            targets |= Knight.knightTargets(ls1b);
        }

        return targets;
    }

    private long kingTargets(long kings)
    {
        long targets = 0;
        while (kings != 0)
        {
            int ls1b = BitScan.ls1b(kings);
            kings &= ~shift(1L, ls1b);
            targets |= King.kingTargets(ls1b);
        }
        return targets;
    }

    private long rookTargets(long rooks, long occupied)
    {
        long attacks = 0;
        while (rooks != 0)
        {
            int ls1b = BitScan.ls1b(rooks);
            rooks &= ~shift(1L, ls1b);
            attacks |= MagicCache.getInstance().rookAttacks(ls1b, occupied);
        }
        return attacks;
    }

    private long bishopTargets(long bishops, long occupied)
    {
        long attacks = 0;
        while (bishops != 0)
        {
            int ls1b = BitScan.ls1b(bishops);
            bishops &= ~shift(1L, ls1b);
            attacks |= MagicCache.getInstance().bishopAttacks(ls1b, occupied);
        }
        return attacks;
    }

    private long queenTargets(long queens, long occupied)
    {
        return bishopTargets(queens, occupied) | rookTargets(queens, occupied);
    }

    private long whitePawnSinglePushTargets(long whitePawns, long empty)
    {
        return northOne(whitePawns) & empty;
    }

    private long blackPawnSinglePushTargets(long blackPawns, long empty)
    {
        return southOne(blackPawns) & empty;
    }

    private long whitePawnDoublePushTargets(long whitePawns, long empty)
    {
        long singlePush = whitePawnSinglePushTargets(whitePawns, empty);
        return whitePawnSinglePushTargets(singlePush, empty) & BB_RANK_4;
    }

    private long blackPawnDoublePushTargets(long blackPawns, long empty)
    {
        long singlePush = blackPawnSinglePushTargets(blackPawns, empty);
        return blackPawnSinglePushTargets(singlePush, empty) & BB_RANK_5;
    }

    private long whitePawnsEastAttackTargets(long whitePawns)
    {
        return NE1(whitePawns);
    }

    private long whitePawnsWestAttackTargets(long whitePawns)
    {
        return NW1(whitePawns);
    }

    private long blackPawnsEastAttackTargets(long blackPawns)
    {
        return SE1(blackPawns);
    }

    private long blackPawnsWestAttackTargets(long blackPawns)
    {
        return SW1(blackPawns);
    }

    private List<Short> pseudoMovesWhite()
    {
        List<Short> moves = new ArrayList<>();
        long opponentPieces = blackPieces();
        long occupied = occupied();

        moves.addAll(pseudoBishopMoves(WB, opponentPieces, occupied));
        moves.addAll(pseudoRookMoves(WR, opponentPieces, occupied));
        moves.addAll(pseudoQueenMoves(WQ, opponentPieces, occupied));
        moves.addAll(pseudoKingMoves(WK, opponentPieces, occupied));
        moves.addAll(pseudoKnightMoves(WN, opponentPieces, occupied));

        return moves;
    }

    private List<Short> pseudoKingMoves(long king, long opponentPieces, long occupied)
    {
        return pseudoNonSlidingMoves(king, opponentPieces, occupied, King::kingTargets);
    }

    private List<Short> pseudoKnightMoves(long knights, long opponentPieces, long occupied)
    {
        return pseudoNonSlidingMoves(knights, opponentPieces, occupied, Knight::knightTargets);
    }

    private List<Short> pseudoRookMoves(long rooks, long opponentPieces, long occupied)
    {
        return pseudoSlidingMoves(rooks, opponentPieces, occupied, MagicCache.getInstance()::rookAttacks);
    }

    private List<Short> pseudoBishopMoves(long bishops, long opponentPieces, long occupied)
    {
        return pseudoSlidingMoves(bishops, opponentPieces, occupied, MagicCache.getInstance()::bishopAttacks);
    }

    private List<Short> pseudoQueenMoves(long queens, long opponentPieces, long occupied)
    {
        return pseudoSlidingMoves(queens, opponentPieces, occupied, MagicCache.getInstance()::queenAttacks);
    }

    private List<Short> pseudoWhitePawnMoves(long whitePawns, long opponentPieces, long occupied)
    {
        List<Short> moves = new ArrayList<>();

        long empty = emptySquares();
        long blackPieces = blackPieces();

        long singlePushes = whitePawnSinglePushTargets(whitePawns, empty) & ~BB_RANK_8;
        long doublePushes = whitePawnDoublePushTargets(whitePawns, empty);
        long attacksNE = whitePawnsEastAttackTargets(whitePawns) & blackPieces & ~BB_RANK_8;
        long attacksNW = whitePawnsWestAttackTargets(whitePawns) & blackPieces & ~BB_RANK_8;

        long promotionsNE = whitePawnsEastAttackTargets(whitePawns) & BB_RANK_8;
        long promotionsNW = whitePawnsWestAttackTargets(whitePawns) & BB_RANK_8;
        long promotionAttacksNE = whitePawnsEastAttackTargets(whitePawns) & blackPieces & BB_RANK_8;
        long promotionAttacksNW = whitePawnsWestAttackTargets(whitePawns) & blackPieces & BB_RANK_8;

        addPawnMoves(singlePushes, Direction.NORTH, Move.QUIET_MOVE, moves);
        addPawnMoves(doublePushes, Direction.NORTH * 2, Move.DOUBLE_PAWN_PUSH, moves);
        addPawnMoves(attacksNE, Direction.NORTH_EAST, Move.CAPTURES, moves);
        addPawnMoves(attacksNW, Direction.NORTH_WEST, Move.CAPTURES, moves);

        addPawnPromotions(promotionsNE, Direction.NORTH_EAST, false, moves);
        addPawnPromotions(promotionsNW, Direction.NORTH_WEST, false, moves);
        addPawnPromotions(promotionAttacksNE, Direction.NORTH_EAST, true, moves);
        addPawnPromotions(promotionAttacksNW, Direction.NORTH_WEST, true, moves);

        return moves;
    }

    private List<Short> pseudoBlackPawnMoves(long blackPawns, long opponentPieces, long occupied)
    {
        List<Short> moves = new ArrayList<>();

        long empty = emptySquares();
        long whitePieces = whitePieces();

        long singlePushes = blackPawnSinglePushTargets(blackPawns, empty) & ~BB_RANK_1;
        long doublePushes = blackPawnDoublePushTargets(blackPawns, empty);
        long attacksSE = blackPawnsEastAttackTargets(blackPawns) & whitePieces & ~BB_RANK_1;
        long attacksSW = blackPawnsWestAttackTargets(blackPawns) & whitePieces & ~BB_RANK_1;

        long promotionsSE = blackPawnsEastAttackTargets(blackPawns) & BB_RANK_1;
        long promotionsSW = blackPawnsWestAttackTargets(blackPawns) & BB_RANK_1;
        long promotionAttacksSE = blackPawnsEastAttackTargets(blackPawns) & whitePieces & BB_RANK_1;
        long promotionAttacksSW = blackPawnsWestAttackTargets(blackPawns) & whitePieces & BB_RANK_1;

        addPawnMoves(singlePushes, Direction.SOUTH, Move.QUIET_MOVE, moves);
        addPawnMoves(doublePushes, Direction.SOUTH * 2, Move.DOUBLE_PAWN_PUSH, moves);
        addPawnMoves(attacksSE, Direction.SOUTH_EAST, Move.CAPTURES, moves);
        addPawnMoves(attacksSW, Direction.SOUTH_WEST, Move.CAPTURES, moves);

        addPawnPromotions(promotionsSE, Direction.SOUTH_EAST, false, moves);
        addPawnPromotions(promotionsSW, Direction.SOUTH_WEST, false, moves);
        addPawnPromotions(promotionAttacksSE, Direction.SOUTH_EAST, true, moves);
        addPawnPromotions(promotionAttacksSW, Direction.SOUTH_WEST, true, moves);

        return moves;
    }

    private void addPawnPromotions(long targetSquares, int shift, boolean isCapture, List<Short> moves)
    {
        while (targetSquares != 0)
        {
            int toSquare = BitScan.ls1b(targetSquares);
            int fromSquare = toSquare - shift;
            targetSquares &= ~BoardUtil.squareBB(toSquare);

            if (isCapture)
            {
                moves.add(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.move(fromSquare, toSquare, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.move(fromSquare, toSquare, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.move(fromSquare, toSquare, Move.QUEEN_PROMO_CAPTURE));
            }
            else
            {
                moves.add(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMOTION));
                moves.add(Move.move(fromSquare, toSquare, Move.BISHOP_PROMOTION));
                moves.add(Move.move(fromSquare, toSquare, Move.ROOK_PROMOTION));
                moves.add(Move.move(fromSquare, toSquare, Move.QUEEN_PROMOTION));
            }
        }
    }

    private void addPawnMoves(long targetSquares, int shift, short moveType, List<Short> moves)
    {
        while (targetSquares != 0)
        {
            int toSquare = BitScan.ls1b(targetSquares);
            int fromSquare = toSquare - shift;
            targetSquares &= ~BoardUtil.squareBB(toSquare);

            if (Move.isPromotion(moveType))
            {
                if (Move.isCapture(moveType))
                {

                    moves.add(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMO_CAPTURE));
                    moves.add(Move.move(fromSquare, toSquare, Move.BISHOP_PROMO_CAPTURE));
                    moves.add(Move.move(fromSquare, toSquare, Move.ROOK_PROMO_CAPTURE));
                    moves.add(Move.move(fromSquare, toSquare, Move.QUEEN_PROMO_CAPTURE));
                }
                else
                {
                    moves.add(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMOTION));
                    moves.add(Move.move(fromSquare, toSquare, Move.BISHOP_PROMOTION));
                    moves.add(Move.move(fromSquare, toSquare, Move.ROOK_PROMOTION));
                    moves.add(Move.move(fromSquare, toSquare, Move.QUEEN_PROMOTION));
                }
            }
            else
            {
                moves.add(Move.move(fromSquare, toSquare, moveType));
            }
        }
    }

    private List<Short> pseudoSlidingMoves(long sliders, long opponentPieces, long occupied, BiFunction<Integer, Long, Long> magicFn)
    {
        List<Short> moves = new ArrayList<>();
        while (sliders != 0)
        {
            int sliderSq = BitScan.ls1b(sliders);
            sliders &= ~shift(1L, sliderSq);
            long attackSet = magicFn.apply(sliderSq, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                moves.add(Move.move(sliderSq, toSquare, moveType));
            }
        }
        return moves;
    }

    private List<Short> pseudoNonSlidingMoves(long fromBB, long opponentPieces, long occupied, Function<Integer, Long> fn)
    {
        List<Short> moves = new ArrayList<>();
        while (fromBB != 0)
        {
            int fromSq = BitScan.ls1b(fromBB);
            fromBB &= ~shift(1L, fromSq);
            long attackSet = fn.apply(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                moves.add(Move.move(fromSq, toSquare, moveType));
            }
        }
        return moves;
    }

    private long shift(long x, int s)
    {
        return (s > 0) ? (x << s) : (x >>> -s);
    }

    private long northOne(long bb)
    {
        return shift(bb, BOARD_DIM);
    }

    private long southOne(long bb)
    {
        return shift(bb, -BOARD_DIM);
    }

    private long eastOne(long bb)
    {
        return shift(bb, 1) & ~BB_FILE_A;
    }

    private long westOne(long bb)
    {
        return shift(bb, -1) & ~BB_FILE_H;
    }

    private long NE1(long bb)
    {
        return shift(bb, 9) & ~BB_FILE_A;
    }

    private long NW1(long bb)
    {
        return shift(bb, 7) & ~BB_FILE_H;
    }

    private long SE1(long bb)
    {
        return shift(bb, -7) & ~BB_FILE_A;
    }

    private long SW1(long bb)
    {
        return shift(bb, -9) & ~BB_FILE_H;
    }

    private boolean intersects(long a, long b)
    {
        return (a & b) != 0;
    }

    public void debug()
    {
//        pseudoKingMoves(BK, whitePieces(), occupied()).forEach(s -> System.out.println(Move.moveString(s)));
//        pseudoKnightMoves(WN, blackPieces(), occupied()).forEach(s -> System.out.println(Move.moveString(s)));
        // pseudoMovesWhite().forEach(s -> System.out.println(Move.moveString(s)));
        pseudoWhitePawnMoves(WP,blackPieces(),occupied()).forEach(s -> System.out.println(Move.moveString(s)));
    }
}
