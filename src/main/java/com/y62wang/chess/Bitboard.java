package com.y62wang.chess;

import com.y62wang.chess.bits.BitScan;
import com.y62wang.chess.bits.Endianess;
import com.y62wang.chess.bits.PopulationCount;
import com.y62wang.chess.magic.MagicCache;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.y62wang.chess.BoardConstants.BB_A1;
import static com.y62wang.chess.BoardConstants.BB_A8;
import static com.y62wang.chess.BoardConstants.BB_FILE_A;
import static com.y62wang.chess.BoardConstants.BB_FILE_H;
import static com.y62wang.chess.BoardConstants.BB_H1;
import static com.y62wang.chess.BoardConstants.BB_H8;
import static com.y62wang.chess.BoardConstants.BB_RANK_1;
import static com.y62wang.chess.BoardConstants.BB_RANK_4;
import static com.y62wang.chess.BoardConstants.BB_RANK_5;
import static com.y62wang.chess.BoardConstants.BB_RANK_8;
import static com.y62wang.chess.BoardConstants.BOARD_DIM;
import static com.y62wang.chess.BoardConstants.B_KING_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.BoardConstants.B_KING_CASTLE_ATTACK_MASK;
import static com.y62wang.chess.BoardConstants.B_QUEEN_CASTLE_ATTACK_MASK;
import static com.y62wang.chess.BoardConstants.B_QUEEN_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.BoardConstants.NEW_BOARD_CHARS;
import static com.y62wang.chess.BoardConstants.RANK_3;
import static com.y62wang.chess.BoardConstants.RANK_4;
import static com.y62wang.chess.BoardConstants.RANK_5;
import static com.y62wang.chess.BoardConstants.RANK_6;
import static com.y62wang.chess.BoardConstants.SQ_A1;
import static com.y62wang.chess.BoardConstants.SQ_A8;
import static com.y62wang.chess.BoardConstants.SQ_C1;
import static com.y62wang.chess.BoardConstants.SQ_C8;
import static com.y62wang.chess.BoardConstants.SQ_E1;
import static com.y62wang.chess.BoardConstants.SQ_E8;
import static com.y62wang.chess.BoardConstants.SQ_G1;
import static com.y62wang.chess.BoardConstants.SQ_G8;
import static com.y62wang.chess.BoardConstants.SQ_H1;
import static com.y62wang.chess.BoardConstants.SQ_H8;
import static com.y62wang.chess.BoardConstants.W_KING_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.BoardConstants.W_KING_CASTLE_ATTACK_MASK;
import static com.y62wang.chess.BoardConstants.W_QUEEN_CASTLE_ATTACK_MASK;
import static com.y62wang.chess.BoardConstants.W_QUEEN_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.BoardUtil.*;

public class Bitboard
{

    public static final int BOARD_WIDTH = 8;
    public static final int SIZE = 64;

    private static final int KING = 5;
    private static final int QUEEN = 4;
    private static final int ROOK = 3;
    private static final int BISHOP = 2;
    private static final int KNIGHT = 1;
    private static final int PAWN = 0;

    private static final int WK_CASTLE_SHIFT = 0;
    private static final int WQ_CASTLE_SHIFT = 1;
    private static final int BK_CASTLE_SHIFT = 2;
    private static final int BQ_CASTLE_SHIFT = 3;
    private static final short WK_CASTLE_MASK = 1;
    private static final short WQ_CASTLE_MASK = 2;
    private static final short BK_CASTLE_MASK = 4;
    private static final short BQ_CASTLE_MASK = 8;
    private static final short FULL_CASTLE_RIGHT = (1 << 5) - 1;
    public static final int NO_EP_TARGET = -1;
    private short castleRights;

    public static byte WHITE = 0;
    public static byte BLACK = 1;

    private int epFileIndex = NO_EP_TARGET;
    private byte turn;
    private long[][] pieces;
    public short rootMove;

    public Bitboard(long[][] boardPieces, byte turn, int epFileIndex, short castleRights)
    {
        this.pieces = boardPieces;
        this.turn = turn;
        this.epFileIndex = epFileIndex;
        this.castleRights = castleRights;
    }

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
        assignPiece(CharacterUtilities.toLittleEndianBoard(sb.toString().toCharArray()));
        turn = ( byte ) (tokens[1].equalsIgnoreCase("w") ? 0 : 1);
        castleRights |= tokens[2].contains("K") ? 1 : 0;
        castleRights |= tokens[2].contains("Q") ? (1 << 1) : 0;
        castleRights |= tokens[2].contains("k") ? (1 << 2) : 0;
        castleRights |= tokens[2].contains("q") ? (1 << 3) : 0;
        epFileIndex = tokens[3].equals("-") ? -1 : Integer.parseInt(tokens[3]) - 1;
    }

    public Bitboard(char[] board)
    {
        assignPiece(board);
        castleRights = FULL_CASTLE_RIGHT;
    }

    private void assignPiece(final char[] board)
    {
        pieces = new long[2][6];

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
                    pieces[BLACK][ROOK] = pieces[BLACK][ROOK] | pos;
                    break;
                case 'n':
                    pieces[BLACK][KNIGHT] = pieces[BLACK][KNIGHT] | pos;
                    break;
                case 'b':
                    pieces[BLACK][BISHOP] = pieces[BLACK][BISHOP] | pos;
                    break;
                case 'q':
                    pieces[BLACK][QUEEN] = pieces[BLACK][QUEEN] | pos;
                    break;
                case 'k':
                    pieces[BLACK][KING] = pieces[BLACK][KING] | pos;
                    break;
                case 'p':
                    pieces[BLACK][PAWN] = pieces[BLACK][PAWN] | pos;
                    break;
                case 'R':
                    pieces[WHITE][ROOK] = pieces[WHITE][ROOK] | pos;
                    break;
                case 'N':
                    pieces[WHITE][KNIGHT] = pieces[WHITE][KNIGHT] | pos;
                    break;
                case 'B':
                    pieces[WHITE][BISHOP] = pieces[WHITE][BISHOP] | pos;
                    break;
                case 'Q':
                    pieces[WHITE][QUEEN] = pieces[WHITE][QUEEN] | pos;
                    break;
                case 'K':
                    pieces[WHITE][KING] = pieces[WHITE][KING] | pos;
                    break;
                case 'P':
                    pieces[WHITE][PAWN] = pieces[WHITE][PAWN] | pos;
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
                                sb.append(" ").append(1 + i / 8).append("   ");
                            }
                            if (1 == (1 & (BR() >>> i)))
                            {
                                sb.append('r');
                            }
                            else if (1 == (1 & (BB() >>> i)))
                            {
                                sb.append('b');
                            }
                            else if (1 == (1 & (BN() >>> i)))
                            {
                                sb.append('n');
                            }
                            else if (1 == (1 & (BQ() >>> i)))
                            {
                                sb.append('q');
                            }
                            else if (1 == (1 & (BK() >>> i)))
                            {
                                sb.append('k');
                            }
                            else if (1 == (1 & (BP() >>> i)))
                            {
                                sb.append('p');
                            }
                            else if (1 == (1 & (WR() >>> i)))
                            {
                                sb.append('R');
                            }
                            else if (1 == (1 & (WB() >>> i)))
                            {
                                sb.append('B');
                            }
                            else if (1 == (1 & (WN() >>> i)))
                            {
                                sb.append('N');
                            }
                            else if (1 == (1 & (WQ() >>> i)))
                            {
                                sb.append('Q');
                            }
                            else if (1 == (1 & (WK() >>> i)))
                            {
                                sb.append('K');
                            }
                            else if (1 == (1 & (WP() >>> i)))
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

    private byte nextTurn()
    {
        return turn == WHITE ? BLACK : WHITE;
    }

    public boolean isWhiteTurn()
    {
        return turn == WHITE;
    }

    public long occupied()
    {
        return WP() | WN() | WB() | WR() | WK() | WQ() | BP() | BN() | BB() | BR() | BK() | BQ();
    }

    private long emptySquares()
    {
        return ~occupied();
    }

    private long whitePieces()
    {
        return WP() | WN() | WB() | WR() | WK() | WQ();
    }

    private long blackPieces()
    {
        return BP() | BN() | BB() | BR() | BK() | BQ();
    }

    private long whiteTargets(long occupied)
    {
        long targets = kingTargets(WK())
                       | queenTargets(WQ(), occupied & ~BK())
                       | knightTargets(WN())
                       | rookTargets(WR(), occupied & ~BK())
                       | bishopTargets(WB(), occupied & ~BK())
                       | whitePawnsEastAttackTargets(WP())
                       | whitePawnsWestAttackTargets(WP());
        return targets;
    }

    private long blackTargets(long occupied)
    {
        long targets = kingTargets(BK())
                       | queenTargets(BQ(), occupied & ~WK())
                       | knightTargets(BN())
                       | rookTargets(BR(), occupied & ~WK())
                       | bishopTargets(BB(), occupied & ~WK())
                       | blackPawnsEastAttackTargets(BP())
                       | blackPawnsWestAttackTargets(BP());
        return targets;
    }

    public Bitboard makeMove(short move)
    {

        long from = Move.fromSquareBB(move);
        long to = Move.toSquareBB(move);
        assert (intersects(from, occupied()));

        long[][] board = new long[][] {{WP(), WN(), WB(), WR(), WQ(), WK()}, {BP(), BN(), BB(), BR(), BQ(), BK()}};

        int myColor = turn;
        int opponentColor = 1 - myColor;

        if (Move.isPromotion(move))
        {
            removePiece(from, board[myColor]);
            if (Move.isCapture(move))
            {
                removePiece(to, board[opponentColor]);
            }

            if (Move.moveCode(move) == Move.QUEEN_PROMOTION || Move.moveCode(move) == Move.QUEEN_PROMO_CAPTURE)
            {
                board[myColor][QUEEN] |= to;
            }
            else if (Move.moveCode(move) == Move.ROOK_PROMOTION || Move.moveCode(move) == Move.ROOK_PROMO_CAPTURE)
            {
                board[myColor][ROOK] |= to;
            }
            else if (Move.moveCode(move) == Move.BISHOP_PROMOTION || Move.moveCode(move) == Move.BISHOP_PROMO_CAPTURE)
            {
                board[myColor][BISHOP] |= to;
            }
            else if (Move.moveCode(move) == Move.KNIGHT_PROMOTION || Move.moveCode(move) == Move.KNIGHT_PROMO_CAPTURE)
            {
                board[myColor][KNIGHT] |= to;
            }
        }
        else if (Move.isEnpassant(move))
        {
            if (turn == WHITE)
            {
                removePiece(southOne(to), board[BLACK]);
            }
            else
            {
                removePiece(northOne(to), board[WHITE]);
            }
            movePiece(from, to, board[myColor]);
        }
        else if (Move.isKingCastle(move))
        {
            int toSquare = Move.toSquare(move);
            movePiece(from, to, board[myColor]);
            movePiece(squareBB(toSquare + 1), squareBB(toSquare - 1), board[myColor]);
        }
        else if (Move.isQueenCastle(move))
        {
            int toSquare = Move.toSquare(move);
            movePiece(from, to, board[myColor]);
            movePiece(squareBB(toSquare - 2), squareBB(toSquare + 1), board[myColor]);
        }
        else
        {
            if (Move.isCapture(move))
            {
                removePiece(to, board[opponentColor]);
            }
            movePiece(from, to, board[myColor]);
        }

        short updatedCastleRights = castleRights;

        if (intersects(WK(), from))
        {
            updatedCastleRights = unsetCastleBit(updatedCastleRights, WK_CASTLE_MASK);
            updatedCastleRights = unsetCastleBit(updatedCastleRights, WQ_CASTLE_MASK);
        }
        else if (intersects(WR() & BB_A1, from))
        {
            updatedCastleRights = unsetCastleBit(updatedCastleRights, WQ_CASTLE_MASK);
        }

        else if (intersects(WR() & BB_H1, from))
        {
            updatedCastleRights = unsetCastleBit(updatedCastleRights, WK_CASTLE_MASK);
        }

        else if (intersects(BK(), from))
        {
            updatedCastleRights = unsetCastleBit(updatedCastleRights, BK_CASTLE_MASK);
            updatedCastleRights = unsetCastleBit(updatedCastleRights, BQ_CASTLE_MASK);
        }
        else if (intersects(BR() & BB_A8, from))
        {
            updatedCastleRights = unsetCastleBit(updatedCastleRights, BQ_CASTLE_MASK);
        }

        else if (intersects(BR() & BB_H8, from))
        {
            updatedCastleRights = unsetCastleBit(updatedCastleRights, BK_CASTLE_MASK);
        }

        int newEnPassantTarget = NO_EP_TARGET;
        if (Move.isDoublePawnPush(move))
        {
            newEnPassantTarget = BoardUtil.file(Move.toSquare(move));
        }
        return new Bitboard(board, nextTurn(), newEnPassantTarget, updatedCastleRights);
    }

    private void movePiece(long from, long to, long[] board)
    {
        for (int pieceIndex = 0; pieceIndex < board.length; pieceIndex++)
        {
            long pieces = board[pieceIndex];
            if (intersects(from, pieces))
            {
                board[pieceIndex] = (pieces & ~from) | to;
                return;
            }
        }
    }

    private void removePiece(long target, long[] board)
    {
        for (int pieceIndex = 0; pieceIndex < board.length; pieceIndex++)
        {
            long pieces = board[pieceIndex];
            if (intersects(target, pieces))
            {
                board[pieceIndex] = pieces & ~target;
                return;
            }
        }
    }

    public Set<Short> legalMoves()
    {
        return turn == WHITE ? whiteLegalMoves() : blackLegalMoves();
    }

    public Set<Short> whiteLegalMoves()
    {
        long ownPieces = whitePieces();
        long opponentPieces = blackPieces();
        long occupied = occupied();
        long myKing = WK();
        long attackersToKing = opponentPieces & attackersTo(myKing, occupied);
        long opponentTargets = blackTargets(occupied);
        int numAttackers = PopulationCount.popCount(attackersToKing);

        Set<Short> kingMoves = pseudoKingMoves(myKing, opponentPieces, occupied)
                .stream()
                .filter(move -> !intersects(squareBB(Move.toSquare(move)), opponentTargets))
                .collect(Collectors.toSet());

        Set<Short> castleMoves = pseudoWhiteCastles(occupied);

        if (numAttackers == 2)
        {
            kingMoves.removeAll(castleMoves);
            return kingMoves;
        }

        Set<Short> moves = pseudoWhiteMoves(opponentPieces,occupied);

        // handle pins
        long bqPinners = bishopQueenPinners(myKing, ownPieces, occupied, BQ() | BB());
        long rqPinners = rookQueenPinners(myKing, ownPieces, occupied, BQ() | BR());
        Set<Short> pinRestrictedMoves = getPinRestrictedMoves(myKing, bqPinners | rqPinners, moves);
        moves.removeAll(pinRestrictedMoves);
        moves.removeIf(m -> intersects(myKing, Move.fromSquareBB(m)) && intersects(Move.toSquareBB(m), opponentTargets));

        for (final Short move : moves)
        {
            if ((Move.fromSquareBB(move) & occupied) == 0)
            {
                System.out.println("WHITE Move " + Move.moveString(move) + " " + (Move.fromSquareBB(move) & occupied));
            }
        }

        if (numAttackers == 1)
        {
            int attacker = BitScan.ls1b(attackersToKing);
            long attackerBB = attackersToKing;
            long betweenKingAndAttacker = InBetweenCache.getInstance().inBetweenSet(attacker, BitScan.ls1b(myKing));

            // remove attacker
            // block attacker (sliders only)
            // move the king

            Set<Short> singleAttackerMoves = moves.stream()
                    .filter(m -> (intersects(myKing, Move.fromSquareBB(m)) && isMovingToSafeSquare(m, opponentTargets))
                                 || (Move.isCapture(m) && intersects(attackerBB, Move.toSquareBB(m)))
                                 || ((intersects(attackerBB, blackSliders()) && intersects((Move.toSquareBB(m)), betweenKingAndAttacker)))
                           )
                    .collect(Collectors.toSet());
            singleAttackerMoves.removeAll(pseudoWhiteCastles(occupied));
            return singleAttackerMoves;
        }

        if (intersects(opponentTargets, W_KING_CASTLE_ATTACK_MASK))
        {
            moves.remove(Move.WHITE_KING_CASTLE_MOVE);
        }

        if (intersects(opponentTargets, W_QUEEN_CASTLE_ATTACK_MASK))
        {
            moves.remove(Move.WHITE_QUEEN_CASTLE_MOVE);
        }
        return moves;
    }

    public Set<Short> blackLegalMoves()
    {
        long ownPieces = blackPieces();
        long opponentPieces = whitePieces();
        long occupied = occupied();
        long myKing = BK();
        long attackersToKing = opponentPieces & attackersTo(myKing, occupied);
        long opponentTargets = whiteTargets(occupied);

        int numAttackers = PopulationCount.popCount(attackersToKing);

        Set<Short> kingMoves = pseudoKingMoves(myKing, opponentPieces, occupied)
                .stream()
                .filter(move -> !intersects(squareBB(Move.toSquare(move)), opponentTargets))
                .collect(Collectors.toSet());

//        kingMoves.forEach(s -> System.out.println(Move.moveString(s) + " " + s));
//        Util.printBitboard(opponentTargets);

        Set<Short> castleMoves = pseudoBlackCastles(occupied);

        if (numAttackers == 2)
        {
            kingMoves.removeAll(castleMoves);
            return kingMoves;
        }

        Set<Short> moves = pseudoBlackMoves(opponentPieces,occupied);

        // handle pins
        long bqPinners = bishopQueenPinners(myKing, ownPieces, occupied, WQ() | WB());
        long rqPinners = rookQueenPinners(myKing, ownPieces, occupied, WQ() | WR());
        Set<Short> pinRestrictedMoves = getPinRestrictedMoves(myKing, bqPinners | rqPinners, moves);
        moves.removeAll(pinRestrictedMoves);
        moves.removeIf(m -> intersects(myKing, Move.fromSquareBB(m)) && intersects(Move.toSquareBB(m), opponentTargets));

        if (numAttackers == 1)
        {
            int attacker = BitScan.ls1b(attackersToKing);
            long attackerBB = attackersToKing;
            long betweenKingAndAttacker = InBetweenCache.getInstance().inBetweenSet(attacker, BitScan.ls1b(myKing));

            // remove attacker
            // block attacker (sliders only)
            // move the king

            Set<Short> singleAttackerMoves = moves.stream()
                    .filter(m -> (intersects(myKing, Move.fromSquareBB(m)) && isMovingToSafeSquare(m, opponentTargets))
                                 || (Move.isCapture(m) && intersects(attackerBB, Move.toSquareBB(m)))
                                 || ((intersects(attackerBB, whiteSliders()) && intersects((Move.toSquareBB(m)), betweenKingAndAttacker)))
                           )
                    .collect(Collectors.toSet());
            singleAttackerMoves.removeAll(pseudoBlackCastles(occupied));
            return singleAttackerMoves;
        }

        if (intersects(opponentTargets, B_KING_CASTLE_ATTACK_MASK))
        {
            moves.remove(Move.BLACK_KING_CASTLE_MOVE);
        }

        if (intersects(opponentTargets, B_QUEEN_CASTLE_ATTACK_MASK))
        {
            moves.remove(Move.BLACK_QUEEN_CASTLE_MOVE);
        }

        return moves;
    }

    private long whiteSliders()
    {
        return WB() | WQ() | WR();
    }

    private long blackSliders()
    {
        return BB() | BQ() | BR();
    }

    private boolean isMovingToSafeSquare(final Short move, final long attackTargets)
    {
        return !intersects(squareBB(Move.toSquare(move)), attackTargets);
    }

    private Set<Short> getPinRestrictedMoves(long king, long pinners, Set<Short> pseudoMoves)
    {
        Set<Short> restrictedMoves = new HashSet<>();
        while (pinners != 0)
        {
            int pinner = BitScan.ls1b(pinners);
            long pinnerBB = shift(1L, pinner);
            long inBetweenSquares = InBetweenCache.getInstance().inBetweenSet(pinner, BitScan.ls1b(king));
            pseudoMoves.stream()
                    .filter(m -> intersects(squareBB(Move.fromSquare(m)), inBetweenSquares))
                    .filter(m -> !intersects(squareBB(Move.toSquare(m)), pinnerBB | inBetweenSquares))
                    .forEach(restrictedMoves::add);
            pinners &= ~pinnerBB;
        }
        return restrictedMoves;
    }

    private long rookQueenPinners(long king, long ownPieces, long occupied, long opponentRQ)
    {
        return opponentRQ & xrayRookAttacks(BitScan.ls1b(king), ownPieces, occupied);
    }

    private long bishopQueenPinners(long king, long ownPieces, long occupied, long opponentBQ)
    {
        return opponentBQ & xrayBishopAttacks(BitScan.ls1b(king), ownPieces, occupied);
    }

    private long xrayRookAttacks(int square, long blockers, long occupancy)
    {
        long squareBB = squareBB(square);
        long targets = rookTargets(squareBB, occupancy);
        blockers &= targets;
        return targets ^ rookTargets(squareBB, occupancy ^ blockers);
    }

    private long xrayBishopAttacks(int square, long blockers, long occupancy)
    {
        long squareBB = squareBB(square);
        long targets = bishopTargets(squareBB, occupancy);
        blockers &= targets;
        return targets ^ bishopTargets(squareBB, occupancy ^ blockers);
    }

    private long attackersTo(long squareBB, long occupied)
    {
        return (kingTargets(squareBB) & (BK() | WK()))
               | (knightTargets(squareBB) & (BN() | WN()))
               | (bishopTargets(squareBB, occupied) & (BQ() | WQ() | BB() | WB()))
               | (rookTargets(squareBB, occupied) & (BQ() | WQ() | BR() | WR()))
               | ((whitePawnsEastAttackTargets(squareBB) | whitePawnsWestAttackTargets(squareBB)) & (BP()))
               | ((blackPawnsEastAttackTargets(squareBB) | blackPawnsWestAttackTargets(squareBB)) & (WP()));
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

    private Set<Short> pseudoWhiteMoves(long opponentPieces, long occupied)
    {
        Set<Short> moves = new HashSet<>();

        moves.addAll(pseudoBishopMoves(WB(), opponentPieces, occupied));
        moves.addAll(pseudoRookMoves(WR(), opponentPieces, occupied));
        moves.addAll(pseudoQueenMoves(WQ(), opponentPieces, occupied));
        moves.addAll(pseudoKingMoves(WK(), opponentPieces, occupied));
        moves.addAll(pseudoKnightMoves(WN(), opponentPieces, occupied));
        moves.addAll(pseudoWhitePawnMoves(WP(), opponentPieces, occupied));
        moves.addAll(pseudoWhiteCastles(occupied));
        return moves;
    }

    private Set<Short> pseudoBlackMoves(long opponentPieces, long occupied)
    {
        Set<Short> moves = new HashSet<>();

        moves.addAll(pseudoBishopMoves(BB(), opponentPieces, occupied));
        moves.addAll(pseudoRookMoves(BR(), opponentPieces, occupied));
        moves.addAll(pseudoQueenMoves(BQ(), opponentPieces, occupied));
        moves.addAll(pseudoKingMoves(BK(), opponentPieces, occupied));
        moves.addAll(pseudoKnightMoves(BN(), opponentPieces, occupied));
        moves.addAll(pseudoBlackPawnMoves(BP(), opponentPieces, occupied));
        moves.addAll(pseudoBlackCastles(occupied));
        return moves;
    }

    private Set<Short> pseudoKingMoves(long king, long opponentPieces, long occupied)
    {
        return pseudoNonSlidingMoves(king, opponentPieces, occupied, King::kingTargets);
    }

    private Set<Short> pseudoKnightMoves(long knights, long opponentPieces, long occupied)
    {
        return pseudoNonSlidingMoves(knights, opponentPieces, occupied, Knight::knightTargets);
    }

    private Set<Short> pseudoRookMoves(long rooks, long opponentPieces, long occupied)
    {
        return pseudoSlidingMoves(rooks, opponentPieces, occupied, MagicCache.getInstance()::rookAttacks);
    }

    private Set<Short> pseudoBishopMoves(long bishops, long opponentPieces, long occupied)
    {
        return pseudoSlidingMoves(bishops, opponentPieces, occupied, MagicCache.getInstance()::bishopAttacks);
    }

    private Set<Short> pseudoQueenMoves(long queens, long opponentPieces, long occupied)
    {
        return pseudoSlidingMoves(queens, opponentPieces, occupied, MagicCache.getInstance()::queenAttacks);
    }

    private Set<Short> pseudoWhiteCastles(long occupied)
    {
        Set<Short> moves = new HashSet<>();
        if (((W_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (WK() & squareBB(SQ_E1)) != 0
            && (WR() & squareBB(SQ_H1)) != 0
            && canCastle(WK_CASTLE_MASK))
        {
            moves.add(Move.move(SQ_E1, SQ_G1, Move.KING_CASTLE));
        }

        if ((W_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (WK() & squareBB(SQ_E1)) != 0
            && (WR() & squareBB(SQ_A1)) != 0
            && canCastle(WQ_CASTLE_MASK))
        {
            moves.add(Move.move(SQ_E1, SQ_C1, Move.QUEEN_CASTLE));
        }
        return moves;
    }

    private Set<Short> pseudoBlackCastles(long occupied)
    {
        Set<Short> moves = new HashSet<>();
        if (((B_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (BK() & squareBB(SQ_E8)) != 0
            && (BR() & squareBB(SQ_H8)) != 0
            && this.canCastle(BK_CASTLE_MASK))
        {
            moves.add(Move.move(SQ_E8, SQ_G8, Move.KING_CASTLE));
        }

        if ((B_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (BK() & squareBB(SQ_E8)) != 0
            && (BR() & squareBB(SQ_A8)) != 0
            && this.canCastle(BQ_CASTLE_MASK))
        {
            moves.add(Move.move(SQ_E8, SQ_C8, Move.QUEEN_CASTLE));
        }
        return moves;
    }

    private Set<Short> pseudoWhitePawnMoves(long whitePawns, long opponentPieces, long occupied)
    {
        Set<Short> moves = new HashSet<>();

        long empty = ~occupied;

        long singlePushes = whitePawnSinglePushTargets(whitePawns, empty) & ~BB_RANK_8;
        long doublePushes = whitePawnDoublePushTargets(whitePawns, empty);
        long attacksNE = whitePawnsEastAttackTargets(whitePawns) & opponentPieces & ~BB_RANK_8;
        long attacksNW = whitePawnsWestAttackTargets(whitePawns) & opponentPieces & ~BB_RANK_8;

        long promotionsPush = whitePawnSinglePushTargets(whitePawns, empty) & BB_RANK_8;
        long promotionAttacksNE = whitePawnsEastAttackTargets(whitePawns) & opponentPieces & BB_RANK_8;
        long promotionAttacksNW = whitePawnsWestAttackTargets(whitePawns) & opponentPieces & BB_RANK_8;

        addPawnMoves(singlePushes, Direction.NORTH, Move.QUIET_MOVE, moves);
        addPawnMoves(doublePushes, Direction.NORTH * 2, Move.DOUBLE_PAWN_PUSH, moves);
        addPawnMoves(attacksNE, Direction.NORTH_EAST, Move.CAPTURES, moves);
        addPawnMoves(attacksNW, Direction.NORTH_WEST, Move.CAPTURES, moves);

        addPawnPromotions(promotionsPush, Direction.NORTH, false, moves);
        addPawnPromotions(promotionAttacksNE, Direction.NORTH_EAST, true, moves);
        addPawnPromotions(promotionAttacksNW, Direction.NORTH_WEST, true, moves);

        addEnPassantForWhite(occupied, moves);
        return moves;
    }

    private Set<Short> pseudoBlackPawnMoves(long blackPawns, long opponentPieces, long occupied)
    {
        Set<Short> moves = new HashSet<>();

        long empty = ~occupied;

        long singlePushes = blackPawnSinglePushTargets(blackPawns, empty) & ~BB_RANK_1;
        long doublePushes = blackPawnDoublePushTargets(blackPawns, empty);
        long attacksSE = blackPawnsEastAttackTargets(blackPawns) & opponentPieces & ~BB_RANK_1;
        long attacksSW = blackPawnsWestAttackTargets(blackPawns) & opponentPieces & ~BB_RANK_1;

        long promotionsPush = blackPawnSinglePushTargets(blackPawns, empty) & BB_RANK_1;
        long promotionAttacksSE = blackPawnsEastAttackTargets(blackPawns) & opponentPieces & BB_RANK_1;
        long promotionAttacksSW = blackPawnsWestAttackTargets(blackPawns) & opponentPieces & BB_RANK_1;

        addPawnMoves(singlePushes, Direction.SOUTH, Move.QUIET_MOVE, moves);
        addPawnMoves(doublePushes, Direction.SOUTH * 2, Move.DOUBLE_PAWN_PUSH, moves);
        addPawnMoves(attacksSE, Direction.SOUTH_EAST, Move.CAPTURES, moves);
        addPawnMoves(attacksSW, Direction.SOUTH_WEST, Move.CAPTURES, moves);

        addPawnPromotions(promotionsPush, Direction.SOUTH, false, moves);
        addPawnPromotions(promotionAttacksSE, Direction.SOUTH_EAST, true, moves);
        addPawnPromotions(promotionAttacksSW, Direction.SOUTH_WEST, true, moves);

        addEnPassantForBlack(occupied, moves);

        return moves;
    }

    private void addPawnPromotions(long targetSquares, int shift, boolean isCapture, Set<Short> moves)
    {
        while (targetSquares != 0)
        {
            int toSquare = BitScan.ls1b(targetSquares);
            int fromSquare = toSquare - shift;
            targetSquares &= ~squareBB(toSquare);

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

    private void addEnPassantForWhite(long occupied, Set<Short> moves)
    {
        int enpassantFile = this.epFileIndex;
        if (enpassantFile < 0)
        {
            return;
        }

        long targetPawnBB = squareBB(enpassantFile, RANK_5);
        long targetSquareBB = squareBB(enpassantFile, RANK_6);
        int targetSquare = square(enpassantFile, RANK_6);

        if (intersects(targetPawnBB, BP()) && intersects(eastOne(WP()), targetPawnBB) && !intersects(targetSquareBB, occupied))
        {
            moves.add(Move.move(targetSquare + Direction.SOUTH_WEST, targetSquare, Move.EP_CAPTURE));
        }

        if (intersects(targetPawnBB, BP()) && intersects(westOne(WP()), targetPawnBB) && !intersects(targetSquareBB, occupied))
        {
            moves.add(Move.move(targetSquare + Direction.SOUTH_EAST, targetSquare, Move.EP_CAPTURE));
        }
    }

    private void addEnPassantForBlack(long occupied, Set<Short> moves)
    {
        int enpassantFile = this.epFileIndex;
        if (enpassantFile < 0)
        {
            return;
        }

        long targetPawnBB = squareBB(enpassantFile, RANK_4);
        long targetSquareBB = squareBB(enpassantFile, RANK_3);
        int targetSquare = square(enpassantFile, RANK_3);

        if (intersects(targetPawnBB, WP()) && intersects(eastOne(BP()), targetPawnBB) && !intersects(targetSquareBB, occupied))
        {
            moves.add(Move.move(targetSquare + Direction.NORTH_WEST, targetSquare, Move.EP_CAPTURE));
        }

        if (intersects(targetPawnBB, WP()) && intersects(westOne(BP()), targetPawnBB) && !intersects(targetSquareBB, occupied))
        {
            moves.add(Move.move(targetSquare + Direction.NORTH_EAST, targetSquare, Move.EP_CAPTURE));
        }
    }

    private void addPawnMoves(long targetSquares, int shift, short moveType, Set<Short> moves)
    {
        while (targetSquares != 0)
        {
            int toSquare = BitScan.ls1b(targetSquares);
            int fromSquare = toSquare - shift;
            targetSquares &= ~squareBB(toSquare);

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

    private Set<Short> pseudoSlidingMoves(long sliders, long opponentPieces, long occupied, BiFunction<Integer, Long, Long> magicFn)
    {
        Set<Short> moves = new HashSet<>();
        while (sliders != 0)
        {
            int sliderSq = BitScan.ls1b(sliders);
            sliders &= ~shift(1L, sliderSq);
            long attackSet = magicFn.apply(sliderSq, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~squareBB(toSquare);
                short moveType = intersects(squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                moves.add(Move.move(sliderSq, toSquare, moveType));
            }
        }
        return moves;
    }

    private Set<Short> pseudoNonSlidingMoves(long fromBB, long opponentPieces, long occupied, Function<Integer, Long> fn)
    {
        Set<Short> moves = new HashSet<>();
        while (fromBB != 0)
        {
            int fromSq = BitScan.ls1b(fromBB);
            fromBB &= ~shift(1L, fromSq);
            long attackSet = fn.apply(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~squareBB(toSquare);
                short moveType = intersects(squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
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

    private boolean canCastle(short castleMask)
    {
        return (castleRights & castleMask) != 0;
    }

    private short setCastleBit(short castleRights, short castleMask)
    {
        return castleRights |= castleMask;
    }

    private short unsetCastleBit(short castleRights, short castleMask)
    {
        return castleRights &= ~castleMask;
    }

    private long WK()
    {
        return pieces[WHITE][KING];
    }

    private long WQ()
    {
        return pieces[WHITE][QUEEN];
    }

    private long WR()
    {
        return pieces[WHITE][ROOK];
    }

    private long WB()
    {
        return pieces[WHITE][BISHOP];
    }

    private long WN()
    {
        return pieces[WHITE][KNIGHT];
    }

    private long WP()
    {
        return pieces[WHITE][PAWN];
    }

    private long BK()
    {
        return pieces[BLACK][KING];
    }

    private long BQ()
    {
        return pieces[BLACK][QUEEN];
    }

    private long BR()
    {
        return pieces[BLACK][ROOK];
    }

    private long BB()
    {
        return pieces[BLACK][BISHOP];
    }

    private long BN()
    {
        return pieces[BLACK][KNIGHT];
    }

    private long BP()
    {
        return pieces[BLACK][PAWN];
    }

    public void debug()
    {
  //      castleRights = FULL_CASTLE_RIGHT;
        // castleRights = unsetCastleBit(castleRights,WK_CASTLE_MASK);
//        castleRights = unsetCastleBit(castleRights,WQ_CASTLE_MASK);
//        castleRights = unsetCastleBit(castleRights,BK_CASTLE_MASK);
//        castleRights = unsetCastleBit(castleRights,BQ_CASTLE_MASK);
        System.out.println("WK CASTLE " + canCastle(WK_CASTLE_MASK));
        System.out.println("WQ CASTLE " + canCastle(WQ_CASTLE_MASK));
        System.out.println("BK CASTLE " + canCastle(BK_CASTLE_MASK));
        System.out.println("BQ CASTLE " + canCastle(BQ_CASTLE_MASK));
    }
}
