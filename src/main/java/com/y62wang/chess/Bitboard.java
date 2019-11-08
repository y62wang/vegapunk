package com.y62wang.chess;

import com.y62wang.chess.bits.BitScan;
import com.y62wang.chess.bits.PopulationCount;
import com.y62wang.chess.enums.Piece;
import com.y62wang.chess.enums.PieceType;
import com.y62wang.chess.enums.Side;
import com.y62wang.chess.magic.MagicCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import static com.y62wang.chess.BoardConstants.B_QUEEN_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.BoardConstants.CASTLE_ATTACK_MASKS;
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
import static com.y62wang.chess.BoardConstants.W_QUEEN_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.BoardUtil.square;
import static com.y62wang.chess.BoardUtil.squareBB;

public class Bitboard
{
    public static final int MAX_MOVES = 256;
    public static long MAKE_MOVE_TIME = 0;
    public static long LEGAL_MOVE_TIME = 0;

    public static final int BOARD_WIDTH = 8;
    public static final int SIZE = 64;

    private static final short WK_CASTLE_MASK = 1;
    private static final short WQ_CASTLE_MASK = 2;
    private static final short BK_CASTLE_MASK = 4;
    private static final short BQ_CASTLE_MASK = 8;
    private static final short FULL_CASTLE_RIGHT = (1 << 5) - 1;
    public static final int NO_EP_TARGET = -1;

    private static List<Bitboard> history = new ArrayList<>();

    private short fullMoveNumber;
    private short halfMoveClock;
    private Side turn;
    private PieceList pieceList;
    private int epFileIndex = NO_EP_TARGET;
    private short[] potentialMoves;
    private int moveCount;
    private short castleRights;

    public Bitboard(PieceList pieceList, Side turn, int epFileIndex, short castleRights, short halfMoveClock)

    {
        this.pieceList = pieceList;
        this.turn = turn;
        this.epFileIndex = epFileIndex;
        this.castleRights = castleRights;
        this.halfMoveClock = halfMoveClock;

        this.potentialMoves = new short[MAX_MOVES];
        this.moveCount = 0;
    }

    public Bitboard()
    {
        this(CharacterUtilities.toLittleEndianBoard(NEW_BOARD_CHARS));
    }

    public Bitboard(final Bitboard bb)
    {
        this.pieceList = bb.pieceList.copy();
        this.turn = bb.turn;
        this.epFileIndex = bb.epFileIndex;
        this.castleRights = bb.castleRights;
        this.potentialMoves = new short[MAX_MOVES];
        this.moveCount = 0;
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
        turn = (tokens[1].equalsIgnoreCase("w") ? Side.WHITE : Side.BLACK);
        castleRights |= tokens[2].contains("K") ? 1 : 0;
        castleRights |= tokens[2].contains("Q") ? (1 << 1) : 0;
        castleRights |= tokens[2].contains("k") ? (1 << 2) : 0;
        castleRights |= tokens[2].contains("q") ? (1 << 3) : 0;
        epFileIndex = tokens[3].equals("-") ? -1 : Integer.parseInt(tokens[3]) - 1;
        halfMoveClock = tokens.length > 4 ? Short.parseShort(tokens[4]) : 0;
        fullMoveNumber = tokens.length > 5 ? Short.parseShort(tokens[5]) : 0;

        potentialMoves = new short[MAX_MOVES];
    }

    public Bitboard(char[] board)
    {
        assignPiece(board);
        turn = Side.WHITE;
        castleRights = FULL_CASTLE_RIGHT;
        potentialMoves = new short[MAX_MOVES];

    }

    private void assignPiece(final char[] board)
    {
        pieceList = new PieceList();

        if (board.length != SIZE)
        {
            throw new RuntimeException("invalid board size: " + board.length);
        }

        for (int i = 0; i < board.length; i++)
        {
            final char pieceChar = board[i];
            Piece piece = Piece.of(pieceChar);
            if (piece != Piece.NO_PIECE)
            {
                pieceList.addPiece(piece, i);
            }
        }
    }

    @Override
    public String toString()
    {
        return pieceList.toString();
    }

    private Side nextTurn()
    {
        return turn == Side.WHITE ? Side.BLACK : Side.WHITE;
    }

    public boolean isWhiteTurn()
    {
        return turn == Side.WHITE;
    }

    public long occupied()
    {
        return WP() | WN() | WB() | WR() | WK() | WQ() | BP() | BN() | BB() | BR() | BK() | BQ();
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

    public void unmake() {
        Bitboard bb = history.remove(history.size() - 1);
        this.pieceList = bb.pieceList;
        this.turn = bb.turn;
        this.epFileIndex = bb.epFileIndex;
        this.castleRights = bb.castleRights;
    }


    public void makeMove(short move)
    {
        // Stopwatch started = Stopwatch.createStarted();
        long from = Move.fromSquareBB(move);
        long to = Move.toSquareBB(move);
        int fromSquare = Move.fromSquare(move);
        int toSquare = Move.toSquare(move);

        assert (intersects(from, occupied()));

        short updatedCastleRights = getUpdatedCastleRights(from);

        history.add(new Bitboard(this));
        PieceList copy = this.pieceList;

        if (Move.moveCode(move) == Move.QUIET_MOVE || Move.moveCode(move) == Move.CAPTURES || Move.moveCode(move) == Move.DOUBLE_PAWN_PUSH)
        {
            if (Move.isCapture(move))
            {
                copy.removePiece(toSquare);
            }
            copy.movePiece(fromSquare, toSquare);
        }
        else if (Move.isKingCastle(move))
        {
            copy.movePiece(fromSquare, toSquare);
            copy.movePiece(toSquare + 1, toSquare - 1);
        }
        else if (Move.isQueenCastle(move))
        {
            copy.movePiece(fromSquare, toSquare);
            copy.movePiece(toSquare - 2, toSquare + 1);
        }
        if (Move.isPromotion(move))
        {
            copy.removePiece(fromSquare);
            if (Move.isCapture(move))
            {
                copy.removePiece(toSquare);
            }

            if (Move.moveCode(move) == Move.QUEEN_PROMOTION || Move.moveCode(move) == Move.QUEEN_PROMO_CAPTURE)
            {
                copy.addPiece(turn, PieceType.QUEEN, toSquare);
            }
            else if (Move.moveCode(move) == Move.ROOK_PROMOTION || Move.moveCode(move) == Move.ROOK_PROMO_CAPTURE)
            {
                copy.addPiece(turn, PieceType.ROOK, toSquare);
            }
            else if (Move.moveCode(move) == Move.BISHOP_PROMOTION || Move.moveCode(move) == Move.BISHOP_PROMO_CAPTURE)
            {
                copy.addPiece(turn, PieceType.BISHOP, toSquare);
            }
            else if (Move.moveCode(move) == Move.KNIGHT_PROMOTION || Move.moveCode(move) == Move.KNIGHT_PROMO_CAPTURE)
            {
                copy.addPiece(turn, PieceType.KNIGHT, toSquare);
            }
        }
        else if (Move.isEnpassant(move))
        {
            if (turn == Side.WHITE)
            {
                copy.removePiece(toSquare + Direction.SOUTH);
            }
            else
            {
                copy.removePiece(toSquare + Direction.NORTH);
            }
            copy.movePiece(fromSquare, toSquare);
        }

        int newEnPassantTarget = NO_EP_TARGET;
        if (Move.isDoublePawnPush(move))
        {
            newEnPassantTarget = BoardUtil.file(Move.toSquare(move));
        }
        // MAKE_MOVE_TIME += started.elapsed().toNanos();
        this.turn = nextTurn();
        this.pieceList = copy;
        this.epFileIndex = newEnPassantTarget;
        this.castleRights = updatedCastleRights;
    }

    private short getUpdatedCastleRights(final long from)
    {
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
        return updatedCastleRights;
    }

    public int moveCount()
    {
        return moveCount;
    }

    public short[] legalMoves()
    {
        moveCount = 0;
        legalMovesBackup();
        return Arrays.copyOf(potentialMoves, moveCount);
    }

    private void legalMovesBackup()
    {
        long occupied = occupied();
        long ownPieces = turn == Side.WHITE ? whitePieces() : blackPieces();
        long opponentPieces = occupied & ~ownPieces;
        long myKing = pieceList.piecesBB(turn, PieceType.KING);
        long attackers = opponentPieces & attackersTo(myKing, occupied);
        int attackersCount = PopulationCount.popCount(attackers);
        long opponentTargets = turn == Side.WHITE ? blackTargets(occupied) : whiteTargets(occupied);
        long pinners = getPinners(occupied, ownPieces, myKing);
        generateSudoMoves();

        int writeIndex = 0, readIndex = 0;

        int newMoveCount = 0;
        while (readIndex < moveCount)
        {
            if (legal(potentialMoves[readIndex], opponentTargets, pinners, attackers, attackersCount))
            {
                if (readIndex != writeIndex)
                {
                    potentialMoves[writeIndex] = potentialMoves[readIndex];
                    potentialMoves[readIndex] = 0;
                }
                readIndex++;
                writeIndex++;
                newMoveCount++;
            }
            else
            {
                potentialMoves[readIndex] = 0;
                readIndex++;
            }
        }
        moveCount = newMoveCount;
    }

    private long getPinners(final long occupied, final long ownPieces, final long myKing)
    {
        long bqPinners = bishopQueenPinners(myKing, ownPieces, occupied, pieceList.piecesBB(turn.flip(), PieceType.QUEEN) | pieceList.piecesBB(turn.flip(), PieceType.BISHOP));
        long rqPinners = rookQueenPinners(myKing, ownPieces, occupied, pieceList.piecesBB(turn.flip(), PieceType.QUEEN) | pieceList.piecesBB(turn.flip(), PieceType.ROOK));
        return bqPinners | rqPinners;
    }

    private boolean legal(short move, long opponentTargets, long pinners, long attackersToKing, final int attackersCount)
    {
        long king = pieceList.piecesBB(turn,PieceType.KING);
        long from = Move.fromSquareBB(move);
        long to = Move.toSquareBB(move);

        if (intersects(king, from))
        {
            if (Move.isKingCastle(move))
            {
                return !intersects(CASTLE_ATTACK_MASKS[turn.index][0], opponentTargets) && attackersToKing == 0;
            }
            else if (Move.isQueenCastle(move))
            {
                return !intersects(CASTLE_ATTACK_MASKS[turn.index][1], opponentTargets) && attackersToKing == 0;
            }
            return !intersects(to, opponentTargets);
        }

        int kingSq = BitScan.ls1b(king);
        return !isPinned(kingSq, pinners, from, to) && canPreventKingAttack(kingSq, attackersToKing, to, attackersCount);
    }

    private boolean canPreventKingAttack(int kingSq, long sliderAttackers, long moveTo, final int attackersCount)
    {

        if (attackersCount == 0)
        {
            return true;
        }
        else if (attackersCount == 2)
        {
            return false;
        }

        int slider = BitScan.ls1b(sliderAttackers);
        long attackerBB = lshift(1L, slider);
        long inBetweenSquares = InBetweenCache.getInstance().inBetweenSet(slider, kingSq);
        return intersects(moveTo, inBetweenSquares | attackerBB);
    }

    private boolean isPinned(int kingSq, long pinners, long moveFrom, long moveTo)
    {
        while (pinners != 0)
        {
            int pinner = BitScan.ls1b(pinners);
            long pinnerBB = lshift(1L, pinner);
            long inBetweenSquares = InBetweenCache.getInstance().inBetweenSet(pinner, kingSq);
            if (intersects(inBetweenSquares, moveFrom))
            {
                return !intersects(moveTo, inBetweenSquares | pinnerBB);
            }
            pinners &= ~pinnerBB;
        }
        return false;
    }

    private void generateSudoMoves()
    {
        long opponentPieces = turn == Side.WHITE ? blackPieces() : whitePieces();
        long occupied = occupied();
        if (turn == Side.WHITE)
        {
            pseudoWhiteMoves(opponentPieces, occupied);
        }
        else
        {
            pseudoBlackMoves(opponentPieces, occupied);
        }
    }

    private long whiteSliders()
    {
        return WB() | WQ() | WR();
    }

    private long blackSliders()
    {
        return BB() | BQ() | BR();
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
            knights &= ~lshift(1L, ls1b);
            targets |= Knight.targets(ls1b);
        }

        return targets;
    }

    private long kingTargets(long kings)
    {
        long targets = 0;
        while (kings != 0)
        {
            int ls1b = BitScan.ls1b(kings);
            kings &= ~lshift(1L, ls1b);
            targets |= King.targets(ls1b);
        }
        return targets;
    }

    private long rookTargets(long rooks, long occupied)
    {
        long attacks = 0;
        while (rooks != 0)
        {
            int ls1b = BitScan.ls1b(rooks);
            rooks &= ~lshift(1L, ls1b);
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
            bishops &= ~lshift(1L, ls1b);
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

    private void pseudoWhiteMoves(long opponentPieces, long occupied)
    {

        pseudoBishopMoves(WB(), opponentPieces, occupied);
        pseudoRookMoves(WR(), opponentPieces, occupied);
        pseudoQueenMoves(WQ(), opponentPieces, occupied);
        pseudoKingMoves(WK(), opponentPieces, occupied);
        pseudoKnightMoves(WN(), opponentPieces, occupied);
        pseudoWhitePawnMoves(WP(), opponentPieces, occupied);
        pseudoWhiteCastles(occupied);
    }

    private void pseudoBlackMoves(long opponentPieces, long occupied)
    {
        pseudoBishopMoves(BB(), opponentPieces, occupied);
        pseudoRookMoves(BR(), opponentPieces, occupied);
        pseudoQueenMoves(BQ(), opponentPieces, occupied);
        pseudoKingMoves(BK(), opponentPieces, occupied);
        pseudoKnightMoves(BN(), opponentPieces, occupied);
        pseudoBlackPawnMoves(BP(), opponentPieces, occupied);
        pseudoBlackCastles(occupied);
    }

    private void pseudoKingMoves(long kingBB, long opponentPieces, long occupied)
    {
        while (kingBB != 0)
        {
            int fromSq = BitScan.ls1b(kingBB);
            kingBB &= ~lshift(1L, fromSq);
            long attackSet = King.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~squareBB(toSquare);
                short moveType = intersects(squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(fromSq, toSquare, moveType));
            }
        }
    }

    private void pseudoKnightMoves(long knightsBB, long opponentPieces, long occupied)
    {
        while (knightsBB != 0)
        {
            int fromSq = BitScan.ls1b(knightsBB);
            knightsBB &= ~lshift(1L, fromSq);
            long attackSet = Knight.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~squareBB(toSquare);
                short moveType = intersects(squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(fromSq, toSquare, moveType));
            }
        }
    }

    private void pseudoRookMoves(long rooks, long opponentPieces, long occupied)
    {
        while (rooks != 0)
        {
            int rook = BitScan.ls1b(rooks);
            rooks &= ~lshift(1L, rook);
            long attackSet = MagicCache.getInstance().rookAttacks(rook, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~squareBB(toSquare);
                short moveType = intersects(squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(rook, toSquare, moveType));
            }
        }
    }

    private void pseudoBishopMoves(long bishops, long opponentPieces, long occupied)
    {
        while (bishops != 0)
        {
            int rook = BitScan.ls1b(bishops);
            bishops &= ~lshift(1L, rook);
            long attackSet = MagicCache.getInstance().bishopAttacks(rook, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~squareBB(toSquare);
                short moveType = intersects(squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(rook, toSquare, moveType));
            }
        }
    }

    private void pseudoQueenMoves(long queens, long opponentPieces, long occupied)
    {
        while (queens != 0)
        {
            int queen = BitScan.ls1b(queens);
            queens &= ~lshift(1L, queen);

            // rook attacks
            long rookAttackSets = MagicCache.getInstance().rookAttacks(queen, occupied);
            rookAttackSets &= ~occupied | opponentPieces;
            while (rookAttackSets != 0)
            {
                int toSquare = BitScan.ls1b(rookAttackSets);
                rookAttackSets &= ~squareBB(toSquare);
                short moveType = intersects(squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(queen, toSquare, moveType));
            }

            // bishop attacks
            long bishopAttackSets = MagicCache.getInstance().bishopAttacks(queen, occupied);
            bishopAttackSets &= ~occupied | opponentPieces;
            while (bishopAttackSets != 0)
            {
                int toSquare = BitScan.ls1b(bishopAttackSets);
                bishopAttackSets &= ~squareBB(toSquare);
                short moveType = intersects(squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(queen, toSquare, moveType));
            }
        }
    }

    private void pseudoWhiteCastles(long occupied)
    {
        if (((W_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (WK() & squareBB(SQ_E1)) != 0
            && (WR() & squareBB(SQ_H1)) != 0
            && canCastle(WK_CASTLE_MASK))
        {
            addMove(Move.move(SQ_E1, SQ_G1, Move.KING_CASTLE));
        }

        if ((W_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (WK() & squareBB(SQ_E1)) != 0
            && (WR() & squareBB(SQ_A1)) != 0
            && canCastle(WQ_CASTLE_MASK))
        {
            addMove(Move.move(SQ_E1, SQ_C1, Move.QUEEN_CASTLE));
        }
    }

    private void pseudoBlackCastles(long occupied)
    {
        if (((B_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (BK() & squareBB(SQ_E8)) != 0
            && (BR() & squareBB(SQ_H8)) != 0
            && this.canCastle(BK_CASTLE_MASK))
        {
            addMove(Move.move(SQ_E8, SQ_G8, Move.KING_CASTLE));
        }

        if ((B_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (BK() & squareBB(SQ_E8)) != 0
            && (BR() & squareBB(SQ_A8)) != 0
            && this.canCastle(BQ_CASTLE_MASK))
        {
            addMove(Move.move(SQ_E8, SQ_C8, Move.QUEEN_CASTLE));
        }
    }

    private void pseudoWhitePawnMoves(long whitePawns, long opponentPieces, long occupied)
    {
        if (whitePawns == 0)
        {
            return;
        }

        long empty = ~occupied;

        long singlePushes = whitePawnSinglePushTargets(whitePawns, empty) & ~BB_RANK_8;
        long doublePushes = whitePawnDoublePushTargets(whitePawns, empty);
        long attacksNE = whitePawnsEastAttackTargets(whitePawns) & opponentPieces & ~BB_RANK_8;
        long attacksNW = whitePawnsWestAttackTargets(whitePawns) & opponentPieces & ~BB_RANK_8;

        long promotionsPush = whitePawnSinglePushTargets(whitePawns, empty) & BB_RANK_8;
        long promotionAttacksNE = whitePawnsEastAttackTargets(whitePawns) & opponentPieces & BB_RANK_8;
        long promotionAttacksNW = whitePawnsWestAttackTargets(whitePawns) & opponentPieces & BB_RANK_8;

        addPawnMoves(singlePushes, Direction.NORTH, Move.QUIET_MOVE);
        addPawnMoves(doublePushes, Direction.NORTH * 2, Move.DOUBLE_PAWN_PUSH);
        addPawnMoves(attacksNE, Direction.NORTH_EAST, Move.CAPTURES);
        addPawnMoves(attacksNW, Direction.NORTH_WEST, Move.CAPTURES);

        addPawnPromotions(promotionsPush, Direction.NORTH, false);
        addPawnPromotions(promotionAttacksNE, Direction.NORTH_EAST, true);
        addPawnPromotions(promotionAttacksNW, Direction.NORTH_WEST, true);

        addEnPassantForWhite(occupied);
    }

    private void pseudoBlackPawnMoves(long blackPawns, long opponentPieces, long occupied)
    {
        if (blackPawns == 0)
        {
            return;
        }

        long empty = ~occupied;

        long singlePushes = blackPawnSinglePushTargets(blackPawns, empty) & ~BB_RANK_1;
        long doublePushes = blackPawnDoublePushTargets(blackPawns, empty);
        long attacksSE = blackPawnsEastAttackTargets(blackPawns) & opponentPieces & ~BB_RANK_1;
        long attacksSW = blackPawnsWestAttackTargets(blackPawns) & opponentPieces & ~BB_RANK_1;

        long promotionsPush = blackPawnSinglePushTargets(blackPawns, empty) & BB_RANK_1;
        long promotionAttacksSE = blackPawnsEastAttackTargets(blackPawns) & opponentPieces & BB_RANK_1;
        long promotionAttacksSW = blackPawnsWestAttackTargets(blackPawns) & opponentPieces & BB_RANK_1;

        addPawnMoves(singlePushes, Direction.SOUTH, Move.QUIET_MOVE);
        addPawnMoves(doublePushes, Direction.SOUTH * 2, Move.DOUBLE_PAWN_PUSH);
        addPawnMoves(attacksSE, Direction.SOUTH_EAST, Move.CAPTURES);
        addPawnMoves(attacksSW, Direction.SOUTH_WEST, Move.CAPTURES);

        addPawnPromotions(promotionsPush, Direction.SOUTH, false);
        addPawnPromotions(promotionAttacksSE, Direction.SOUTH_EAST, true);
        addPawnPromotions(promotionAttacksSW, Direction.SOUTH_WEST, true);

        addEnPassantForBlack(occupied);
    }

    private void addPawnPromotions(long targetSquares, int shift, boolean isCapture)
    {
        while (targetSquares != 0)
        {
            int toSquare = BitScan.ls1b(targetSquares);
            int fromSquare = toSquare - shift;
            targetSquares &= ~squareBB(toSquare);

            if (isCapture)
            {
                addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMO_CAPTURE));
                addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMO_CAPTURE));
                addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMO_CAPTURE));
                addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMO_CAPTURE));
            }
            else
            {
                addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMOTION));
                addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMOTION));
                addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMOTION));
                addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMOTION));
            }
        }
    }

    private void addEnPassantForWhite(long occupied)
    {
        if (epFileIndex < 0)
        {
            return;
        }

        long targetPawnBB = squareBB(epFileIndex, RANK_5);
        long targetSquareBB = squareBB(epFileIndex, RANK_6);
        int targetSquare = square(epFileIndex, RANK_6);

        if (!intersects(targetPawnBB, BP()) || intersects(targetSquareBB, occupied))
        {
            return;
        }

        if (intersects(eastOne(WP()), targetPawnBB))
        {
            addMove(Move.move(targetSquare + Direction.SOUTH_WEST, targetSquare, Move.EP_CAPTURE));
        }

        if (intersects(westOne(WP()), targetPawnBB))
        {
            addMove(Move.move(targetSquare + Direction.SOUTH_EAST, targetSquare, Move.EP_CAPTURE));
        }
    }

    private void addEnPassantForBlack(long occupied)
    {
        if (epFileIndex < 0)
        {
            return;
        }

        long targetPawnBB = squareBB(epFileIndex, RANK_4);
        long targetSquareBB = squareBB(epFileIndex, RANK_3);
        int targetSquare = square(epFileIndex, RANK_3);

        if (!intersects(targetPawnBB, WP()) || intersects(targetSquareBB, occupied))
        {
            return;
        }

        if (intersects(eastOne(BP()), targetPawnBB))
        {
            addMove(Move.move(targetSquare + Direction.NORTH_WEST, targetSquare, Move.EP_CAPTURE));
        }

        if (intersects(westOne(BP()), targetPawnBB))
        {
            addMove(Move.move(targetSquare + Direction.NORTH_EAST, targetSquare, Move.EP_CAPTURE));
        }
    }

    private void addPawnMoves(long targetSquares, int shift, short moveType)
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
                    addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMO_CAPTURE));
                    addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMO_CAPTURE));
                    addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMO_CAPTURE));
                    addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMO_CAPTURE));
                }
                else
                {
                    addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMOTION));
                    addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMOTION));
                    addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMOTION));
                    addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMOTION));
                }
            }
            else
            {
                addMove(Move.move(fromSquare, toSquare, moveType));
            }
        }
    }

    private long lshift(long x, int s)
    {
        return x << s;
    }

    private long rshift(long x, int s)
    {
        return x >>> s;
    }

    private long northOne(long bb)
    {
        return lshift(bb, BOARD_DIM);
    }

    private long southOne(long bb)
    {
        return rshift(bb, BOARD_DIM);
    }

    private long eastOne(long bb)
    {
        return lshift(bb, 1) & ~BB_FILE_A;
    }

    private long westOne(long bb)
    {
        return rshift(bb, 1) & ~BB_FILE_H;
    }

    private long NE1(long bb)
    {
        return lshift(bb, 9) & ~BB_FILE_A;
    }

    private long NW1(long bb)
    {
        return lshift(bb, 7) & ~BB_FILE_H;
    }

    private long SE1(long bb)
    {
        return rshift(bb, 7) & ~BB_FILE_A;
    }

    private long SW1(long bb)
    {
        return rshift(bb, 9) & ~BB_FILE_H;
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
        castleRights |= castleMask;
        return castleRights;
    }

    private short unsetCastleBit(short castleRights, short castleMask)
    {
        castleRights &= ~castleMask;
        return castleRights;
    }

    private long WK()
    {
        return pieceList.piecesBB(Piece.W_KING);
    }

    private long WQ()
    {
        return pieceList.piecesBB(Piece.W_QUEEN);
    }

    private long WR()
    {
        return pieceList.piecesBB(Piece.W_ROOK);
    }

    private long WB()
    {
        return pieceList.piecesBB(Piece.W_BISHOP);
    }

    private long WN()
    {
        return pieceList.piecesBB(Piece.W_KNIGHT);
    }

    private long WP()
    {
        return pieceList.piecesBB(Piece.W_PAWN);
    }

    private long BK()
    {
        return pieceList.piecesBB(Piece.B_KING);
    }

    private long BQ()
    {
        return pieceList.piecesBB(Piece.B_QUEEN);
    }

    private long BR()
    {
        return pieceList.piecesBB(Piece.B_ROOK);
    }

    private long BB()
    {
        return pieceList.piecesBB(Piece.B_BISHOP);
    }

    private long BN()
    {
        return pieceList.piecesBB(Piece.B_KNIGHT);
    }

    private long BP()
    {
        return pieceList.piecesBB(Piece.B_PAWN);
    }

    private void addMove(short move)
    {
        this.potentialMoves[moveCount++] = move;
    }

    public void debug()
    {
        System.out.println(this.castleRights);
    }
}
