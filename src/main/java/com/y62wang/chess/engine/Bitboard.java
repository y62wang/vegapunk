package com.y62wang.chess.engine;

import com.y62wang.chess.engine.bits.BitScan;
import com.y62wang.chess.engine.bits.PopulationCount;
import com.y62wang.chess.engine.enums.Piece;
import com.y62wang.chess.engine.enums.PieceType;
import com.y62wang.chess.engine.enums.Side;
import com.y62wang.chess.engine.magic.MagicCache;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.y62wang.chess.engine.BoardConstants.BB_A1;
import static com.y62wang.chess.engine.BoardConstants.BB_A8;
import static com.y62wang.chess.engine.BoardConstants.BB_E1;
import static com.y62wang.chess.engine.BoardConstants.BB_E8;
import static com.y62wang.chess.engine.BoardConstants.BB_FILE_A;
import static com.y62wang.chess.engine.BoardConstants.BB_FILE_H;
import static com.y62wang.chess.engine.BoardConstants.BB_H1;
import static com.y62wang.chess.engine.BoardConstants.BB_H8;
import static com.y62wang.chess.engine.BoardConstants.BB_RANK_1;
import static com.y62wang.chess.engine.BoardConstants.BB_RANK_4;
import static com.y62wang.chess.engine.BoardConstants.BB_RANK_5;
import static com.y62wang.chess.engine.BoardConstants.BB_RANK_8;
import static com.y62wang.chess.engine.BoardConstants.BOARD_DIM;
import static com.y62wang.chess.engine.BoardConstants.B_KING_CASTLE_ATTACK_MASK;
import static com.y62wang.chess.engine.BoardConstants.B_KING_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.engine.BoardConstants.B_QUEEN_CASTLE_ATTACK_MASK;
import static com.y62wang.chess.engine.BoardConstants.B_QUEEN_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.engine.BoardConstants.CASTLE_ATTACK_MASKS;
import static com.y62wang.chess.engine.BoardConstants.NEW_BOARD_CHARS;
import static com.y62wang.chess.engine.BoardConstants.RANK_3;
import static com.y62wang.chess.engine.BoardConstants.RANK_4;
import static com.y62wang.chess.engine.BoardConstants.RANK_5;
import static com.y62wang.chess.engine.BoardConstants.RANK_6;
import static com.y62wang.chess.engine.BoardConstants.SQ_A1;
import static com.y62wang.chess.engine.BoardConstants.SQ_A8;
import static com.y62wang.chess.engine.BoardConstants.SQ_C1;
import static com.y62wang.chess.engine.BoardConstants.SQ_C8;
import static com.y62wang.chess.engine.BoardConstants.SQ_D1;
import static com.y62wang.chess.engine.BoardConstants.SQ_D8;
import static com.y62wang.chess.engine.BoardConstants.SQ_E1;
import static com.y62wang.chess.engine.BoardConstants.SQ_E8;
import static com.y62wang.chess.engine.BoardConstants.SQ_F1;
import static com.y62wang.chess.engine.BoardConstants.SQ_F8;
import static com.y62wang.chess.engine.BoardConstants.SQ_G1;
import static com.y62wang.chess.engine.BoardConstants.SQ_G8;
import static com.y62wang.chess.engine.BoardConstants.SQ_H1;
import static com.y62wang.chess.engine.BoardConstants.SQ_H8;
import static com.y62wang.chess.engine.BoardConstants.W_KING_CASTLE_ATTACK_MASK;
import static com.y62wang.chess.engine.BoardConstants.W_KING_CASTLE_CLEAR_MASK;
import static com.y62wang.chess.engine.BoardConstants.W_QUEEN_CASTLE_ATTACK_MASK;
import static com.y62wang.chess.engine.BoardConstants.W_QUEEN_CASTLE_CLEAR_MASK;

@Log4j2
public class Bitboard
{
    public static final int MAX_MOVES_PER_POSITION = 256;
    public static final int MAX_MOVES_PER_GAME = 10000;
    public static long MAKE_MOVE_TIME = 0;
    public static long LEGAL_MOVE_TIME = 0;

    public static final int BOARD_WIDTH = 8;
    public static final int SIZE = 64;

    private static final short WK_CASTLE_MASK = 1;
    private static final short WQ_CASTLE_MASK = 2;
    private static final short BK_CASTLE_MASK = 4;
    private static final short BQ_CASTLE_MASK = 8;
    private static final short FULL_CASTLE_RIGHT = (1 << 4) - 1;
    public static final int NO_EP_TARGET = 0;

    private static final int[] historyStates = new int[MAX_MOVES_PER_GAME];
    private static int stateCount = 0;

    private short fullMoveNumber;
    private short halfMoveClock;
    private Side turn;
    private PieceList pieceList;
    private final short[] potentialMoves;
    private int moveCount;

    // history structure: [3 bits: captured piece] [16 bits: move] [4 bits: EP] [4 bits castle]
    private int irreversibleState;

    public Bitboard(final PieceList pieceList, final Side turn, final int irreversibleState)
    {
        this.pieceList = pieceList;
        this.turn = turn;
        this.irreversibleState = irreversibleState;
        this.potentialMoves = new short[MAX_MOVES_PER_POSITION];
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
        this.irreversibleState = bb.irreversibleState;
        this.potentialMoves = new short[MAX_MOVES_PER_POSITION];
        this.moveCount = 0;
    }

    public Bitboard(final String FEN)
    {
        final StringBuilder sb = new StringBuilder();
        final String[] tokens = FEN.split(" ");
        if (tokens.length < 4)
        {
            throw new RuntimeException("Invalid FEN " + FEN);
        }
        for (int i = 0; i < tokens[0].length(); i++)
        {
            final char c = tokens[0].charAt(i);
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
        this.assignPiece(CharacterUtilities.toLittleEndianBoard(sb.toString().toCharArray()));
        this.turn = (tokens[1].equalsIgnoreCase("w") ? Side.WHITE : Side.BLACK);

        short castleRights = 0;
        castleRights |= tokens[2].contains("K") ? 1 : 0;
        castleRights |= tokens[2].contains("Q") ? (1 << 1) : 0;
        castleRights |= tokens[2].contains("k") ? (1 << 2) : 0;
        castleRights |= tokens[2].contains("q") ? (1 << 3) : 0;
        final int epFile = tokens[3].equals("-") ? 0 : Integer.parseInt(tokens[3]);

        this.irreversibleState = this.getIrreversibleState(castleRights, epFile, 0);

        this.halfMoveClock = tokens.length > 4 ? Short.parseShort(tokens[4]) : 0;
        this.fullMoveNumber = tokens.length > 5 ? Short.parseShort(tokens[5]) : 0;

        this.potentialMoves = new short[MAX_MOVES_PER_POSITION];
    }

    public Bitboard(final char[] board)
    {
        this.assignPiece(board);
        this.turn = Side.WHITE;
        this.irreversibleState = this.getIrreversibleState(FULL_CASTLE_RIGHT, NO_EP_TARGET, 0);
        this.potentialMoves = new short[MAX_MOVES_PER_POSITION];
    }

    public Side getTurn()
    {
        return this.turn;
    }

    private int getIrreversibleState(final int castleRights, final int epFile, final int capturedPiece)
    {
        return castleRights | epFile << 4 | capturedPiece << 24;
    }

    private int getIrreversibleState(final int castleRights, final int epFile, final int capturedPiece, final short move)
    {
        return castleRights | epFile << 4 | (Short.toUnsignedInt(move)) << 8 | capturedPiece << 24;
    }

    public int getCastleRights()
    {
        return this.getCastleRights(this.irreversibleState);
    }

    public int getEPFile()
    {
        return this.getEnPassantFile(this.irreversibleState);
    }

    private short getCastleRights(final int irreversibleState)
    {
        return ( short ) (FULL_CASTLE_RIGHT & irreversibleState);
    }

    private int getEnPassantFile(final int irreversibleState)
    {
        return (irreversibleState >>> 4) & ((1 << 4) - 1);
    }

    private short getMove(final int irreversibleState)
    {
        return ( short ) ((irreversibleState >>> 8) & ((1 << 16) - 1));
    }

    private PieceType getCapturedPiece(final int irreversibleState)
    {
        final int pieceIndex = (irreversibleState >>> 24) & ((1 << 3) - 1);
        return PieceType.of(pieceIndex);
    }

    private void assignPiece(final char[] board)
    {
        this.pieceList = new PieceList();

        if (board.length != SIZE)
        {
            throw new RuntimeException("invalid board size: " + board.length);
        }

        for (int i = 0; i < board.length; i++)
        {
            final char pieceChar = board[i];
            final Piece piece = Piece.of(pieceChar);
            if (piece != Piece.NO_PIECE)
            {
                this.pieceList.addPiece(piece, i);
            }
        }
    }

    @Override
    public String toString()
    {
        return this.pieceList.toString();
    }

    private Side nextTurn()
    {
        return this.turn == Side.WHITE ? Side.BLACK : Side.WHITE;
    }

    public boolean isWhiteTurn()
    {
        return this.turn == Side.WHITE;
    }

    public long occupied()
    {
        return this.pieceList.occupied();
    }

    private long whitePieces()
    {
        return this.WP() | this.WN() | this.WB() | this.WR() | this.WK() | this.WQ();
    }

    private long blackPieces()
    {
        return this.BP() | this.BN() | this.BB() | this.BR() | this.BK() | this.BQ();
    }

    public long targets(final Side side)
    {
        return side == Side.WHITE ? this.whiteTargets(this.occupied()) : this.blackTargets(this.occupied());
    }

    public long pieces(final Side side)
    {
        return this.pieceList.sideBB(side);
    }

    private long whiteTargets(final long occupied)
    {
        final long targets = this.singleKingTargets(this.WK())
                             | this.queenTargets(this.WQ(), occupied & ~this.BK())
                             | this.knightTargets(this.WN())
                             | this.rookTargets(this.WR(), occupied & ~this.BK())
                             | this.bishopTargets(this.WB(), occupied & ~this.BK())
                             | this.whitePawnsEastAttackTargets(this.WP())
                             | this.whitePawnsWestAttackTargets(this.WP());
        return targets;
    }

    private long blackTargets(final long occupied)
    {
        final long targets = this.singleKingTargets(this.BK())
                             | this.queenTargets(this.BQ(), occupied & ~this.WK())
                             | this.knightTargets(this.BN())
                             | this.rookTargets(this.BR(), occupied & ~this.WK())
                             | this.bishopTargets(this.BB(), occupied & ~this.WK())
                             | this.blackPawnsEastAttackTargets(this.BP())
                             | this.blackPawnsWestAttackTargets(this.BP());
        return targets;
    }

    public void unmake()
    {
        try
        {
            this.unmakeMove();
        }
        catch (final Exception ex)
        {
            log.error("failing to unmake move" + this.toString(), ex);
            throw ex;
        }
    }

    public void unmakeMove()
    {
        final int irreversibleState = historyStates[--stateCount];
        final short move = this.getMove(irreversibleState);

        final int fromSq = Move.fromSquare(move);
        final int toSq = Move.toSquare(move);
        this.pieceList.movePiece(toSq, fromSq);

        final int moveCode = Move.moveCode(move);
//        log.info("Unmake " + Move.moveString(Move.move(fromSq,toSq,moveCode)));
        if (moveCode == Move.QUIET_MOVE || moveCode == Move.DOUBLE_PAWN_PUSH)
        {
        }
        else if (moveCode == Move.CAPTURES)
        {
            this.pieceList.addPiece(Piece.of(this.turn, this.getCapturedPiece(irreversibleState)), toSq);
        }
        else if (Move.isPromoCapture(move))
        {
            this.pieceList.removePiece(fromSq);
            this.pieceList.addPiece(Piece.of(this.turn.flip(), PieceType.PAWN), fromSq);
            this.pieceList.addPiece(Piece.of(this.turn, this.getCapturedPiece(irreversibleState)), toSq);
        }
        else if (Move.isPromotion(move))
        {
            this.pieceList.removePiece(fromSq);
            this.pieceList.addPiece(Piece.of(this.turn.flip(), PieceType.PAWN), fromSq);
        }
        else if (Move.isKingCastle(move))
        {
            if (this.turn == Side.BLACK)
            {
                this.pieceList.movePiece(SQ_F1, SQ_H1);
            }
            else
            {
                this.pieceList.movePiece(SQ_F8, SQ_H8);
            }
        }
        else if (Move.isQueenCastle(move))
        {
            if (this.turn == Side.BLACK)
            {
                this.pieceList.movePiece(SQ_D1, SQ_A1);
            }
            else
            {
                this.pieceList.movePiece(SQ_D8, SQ_A8);
            }
        }
        else if (Move.isEnpassant(move))
        {
            final int moveDirection = this.turn == Side.BLACK ? Direction.SOUTH : Direction.NORTH;
            this.pieceList.addPiece(Piece.of(this.turn, PieceType.PAWN), toSq + moveDirection);
        }

        this.turn = this.turn.flip();
        this.irreversibleState = irreversibleState;
    }

    public void makeMove(final String move)
    {
        try
        {
            this.makeMove(Move.of(move));
        }
        catch (final Exception ex)
        {
            log.error("failing to make move", ex);
            throw ex;
        }
    }

    public void makeMove(final short move)
    {
        // Stopwatch started = Stopwatch.createStarted();
        final long from = Move.fromSquareBB(move);
        final int fromSquare = Move.fromSquare(move);
        final int toSquare = Move.toSquare(move);

        assert (this.intersects(from, this.occupied()));

        final short updatedCastleRights = this.getUpdatedCastleRights(from);

        final PieceList copy = this.pieceList;
        final Piece capturedPiece = this.pieceList.onSquare(toSquare);
        historyStates[stateCount++] = this.getIrreversibleState(this.getCastleRights(this.irreversibleState),
                                                                this.getEnPassantFile(this.irreversibleState),
                                                                capturedPiece.type.index, move);

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
                copy.addPiece(this.turn, PieceType.QUEEN, toSquare);
            }
            else if (Move.moveCode(move) == Move.ROOK_PROMOTION || Move.moveCode(move) == Move.ROOK_PROMO_CAPTURE)
            {
                copy.addPiece(this.turn, PieceType.ROOK, toSquare);
            }
            else if (Move.moveCode(move) == Move.BISHOP_PROMOTION || Move.moveCode(move) == Move.BISHOP_PROMO_CAPTURE)
            {
                copy.addPiece(this.turn, PieceType.BISHOP, toSquare);
            }
            else if (Move.moveCode(move) == Move.KNIGHT_PROMOTION || Move.moveCode(move) == Move.KNIGHT_PROMO_CAPTURE)
            {
                copy.addPiece(this.turn, PieceType.KNIGHT, toSquare);
            }
        }
        else if (Move.isEnpassant(move))
        {
            if (this.turn == Side.WHITE)
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
            newEnPassantTarget = BoardUtil.file(Move.toSquare(move)) + 1;
        }
        this.turn = this.nextTurn();
        this.pieceList = copy;
        this.irreversibleState = this.getIrreversibleState(updatedCastleRights, newEnPassantTarget, 0, ( short ) 0);

    }

    private short getUpdatedCastleRights(final long from)
    {
        short updatedCastleRights = this.getCastleRights(this.irreversibleState);

        if (this.intersects(this.WK(), from))
        {
            updatedCastleRights = this.unsetCastleBit(updatedCastleRights, WK_CASTLE_MASK);
            updatedCastleRights = this.unsetCastleBit(updatedCastleRights, WQ_CASTLE_MASK);
        }
        else if (this.intersects(this.WR() & BB_A1, from))
        {
            updatedCastleRights = this.unsetCastleBit(updatedCastleRights, WQ_CASTLE_MASK);
        }

        else if (this.intersects(this.WR() & BB_H1, from))
        {
            updatedCastleRights = this.unsetCastleBit(updatedCastleRights, WK_CASTLE_MASK);
        }

        else if (this.intersects(this.BK(), from))
        {
            updatedCastleRights = this.unsetCastleBit(updatedCastleRights, BK_CASTLE_MASK);
            updatedCastleRights = this.unsetCastleBit(updatedCastleRights, BQ_CASTLE_MASK);
        }
        else if (this.intersects(this.BR() & BB_A8, from))
        {
            updatedCastleRights = this.unsetCastleBit(updatedCastleRights, BQ_CASTLE_MASK);
        }

        else if (this.intersects(this.BR() & BB_H8, from))
        {
            updatedCastleRights = this.unsetCastleBit(updatedCastleRights, BK_CASTLE_MASK);
        }
        return updatedCastleRights;
    }

    public int moveCount()
    {
        return this.moveCount;
    }

    public short[] legalMoves()
    {
        this.moveCount = 0;
        this.legalMoves2();
        return Arrays.copyOf(this.potentialMoves, this.moveCount);
    }

    private void legalMoves2()
    {
        final long occupied = this.occupied();
        final long ownPieces = this.turn == Side.WHITE ? this.whitePieces() : this.blackPieces();
        final long opponentPieces = occupied & ~ownPieces;
        if (this.turn == Side.WHITE)
        {
            this.legalWhiteMoves(opponentPieces, occupied);
        }
        else
        {
            this.legalBlackMoves(opponentPieces, occupied);
        }
    }

    private void legalMovesWithPseudoMoveRemoval()
    {
        final long myKing = this.pieceList.piecesBB(this.turn, PieceType.KING);
        final long occupied = this.occupied();
        final long ownPieces = this.turn == Side.WHITE ? this.whitePieces() : this.blackPieces();
        final long opponentPieces = occupied & ~ownPieces;
        final long kingAttackers = opponentPieces & this.attackersTo(myKing, occupied);
        final int attackersCount = PopulationCount.popCount(kingAttackers);
        final long opponentTargets = this.turn == Side.WHITE ? this.blackTargets(occupied) : this.whiteTargets(occupied);
        final long pinners = this.getPinners(occupied, ownPieces, myKing);

        this.generatePseudoMoves();

        int writeIndex = 0, readIndex = 0;
        int newMoveCount = 0;

        while (readIndex < this.moveCount)
        {
            if (this.legal(this.potentialMoves[readIndex], opponentTargets, pinners, kingAttackers, attackersCount))
            {
                if (readIndex != writeIndex)
                {
                    this.potentialMoves[writeIndex] = this.potentialMoves[readIndex];
                    this.potentialMoves[readIndex] = 0;
                }
                readIndex++;
                writeIndex++;
                newMoveCount++;
            }
            else
            {
                this.potentialMoves[readIndex] = 0;
                readIndex++;
            }
        }
        this.moveCount = newMoveCount;
    }

    private long getPinners(final long occupied, final long ownPieces, final long myKing)
    {
        final long bqPinners = this.bishopQueenPinners(myKing, ownPieces, occupied, this.pieceList.piecesBB(this.turn.flip(), PieceType.QUEEN) | this.pieceList.piecesBB(this.turn.flip(), PieceType.BISHOP));
        final long rqPinners = this.rookQueenPinners(myKing, ownPieces, occupied, this.pieceList.piecesBB(this.turn.flip(), PieceType.QUEEN) | this.pieceList.piecesBB(this.turn.flip(), PieceType.ROOK));
        return bqPinners | rqPinners;
    }

    public boolean isLegal(final short move)
    {
        final long occupied = this.occupied();
        final long ownPieces = this.turn == Side.WHITE ? this.whitePieces() : this.blackPieces();
        final long opponentPieces = occupied & ~ownPieces;
        final long myKing = this.pieceList.piecesBB(this.turn, PieceType.KING);
        final long kingAttackers = opponentPieces & this.attackersTo(myKing, occupied);
        final int attackersCount = PopulationCount.popCount(kingAttackers);
        final long opponentTargets = this.turn == Side.WHITE ? this.blackTargets(occupied) : this.whiteTargets(occupied);
        final long pinners = this.getPinners(occupied, ownPieces, myKing);
        return this.legal(move, opponentTargets, pinners, kingAttackers, attackersCount);
    }

    private boolean legal(final short move, final long opponentTargets, final long pinners, final long attackersToKing, final int attackersCount)
    {
        final long king = this.pieceList.piecesBB(this.turn, PieceType.KING);
        final long from = Move.fromSquareBB(move);
        final long to = Move.toSquareBB(move);

        if (this.intersects(king, from))
        {
            if (Move.isKingCastle(move))
            {
                return !this.intersects(CASTLE_ATTACK_MASKS[this.turn.index][0], opponentTargets) && attackersToKing == 0;
            }
            else if (Move.isQueenCastle(move))
            {
                return !this.intersects(CASTLE_ATTACK_MASKS[this.turn.index][1], opponentTargets) && attackersToKing == 0;
            }
            return !this.intersects(to, opponentTargets);
        }

        final int kingSq = BitScan.ls1b(king);
        return !this.isMovePinned(kingSq, pinners, from, to) && this.canPreventKingAttack(kingSq, attackersToKing, to, attackersCount);
    }

    private boolean canPreventKingAttack(final int kingSq, final long sliderAttackers, final long moveTo, final int attackersCount)
    {
        if (attackersCount == 0)
        {
            return true;
        }
        else if (attackersCount == 2)
        {
            return false;
        }

        final int slider = BitScan.ls1b(sliderAttackers);
        final long attackerBB = this.leftShift(1L, slider);
        final long inBetweenSquares = InBetweenCache.getInstance().inBetweenSet(slider, kingSq);
        return this.intersects(moveTo, inBetweenSquares | attackerBB);
    }

    private boolean isMovePinned(final int kingSq, long pinners, final long moveFrom, final long moveTo)
    {
        while (pinners != 0)
        {
            final int pinner = BitScan.ls1b(pinners);
            final long pinnerBB = this.leftShift(1L, pinner);
            final long inBetweenSquares = InBetweenCache.getInstance().inBetweenSet(pinner, kingSq);
            if (this.intersects(inBetweenSquares, moveFrom))
            {
                return !this.intersects(moveTo, inBetweenSquares | pinnerBB);
            }
            pinners &= ~pinnerBB;
        }
        return false;
    }

    private void generatePseudoMoves()
    {
        final long opponentPieces = this.turn == Side.WHITE ? this.blackPieces() : this.whitePieces();
        final long occupied = this.occupied();
        if (this.turn == Side.WHITE)
        {
            this.pseudoWhiteMoves(opponentPieces, occupied);
        }
        else
        {
            this.pseudoBlackMoves(opponentPieces, occupied);
        }
    }

    private long rookQueenPinners(final long king, final long ownPieces, final long occupied, final long opponentRQ)
    {
        return opponentRQ & this.xrayRookAttacks(BitScan.ls1b(king), ownPieces, occupied);
    }

    private long bishopQueenPinners(final long king, final long ownPieces, final long occupied, final long opponentBQ)
    {
        return opponentBQ & this.xrayBishopAttacks(BitScan.ls1b(king), ownPieces, occupied);
    }

    private long xrayRookAttacks(final int square, long blockers, final long occupancy)
    {
        final long squareBB = BoardUtil.squareBB(square);
        final long targets = this.rookTargets(squareBB, occupancy);
        blockers &= targets;
        return targets ^ this.rookTargets(squareBB, occupancy ^ blockers);
    }

    private long xrayBishopAttacks(final int square, long blockers, final long occupancy)
    {
        final long squareBB = BoardUtil.squareBB(square);
        final long targets = this.bishopTargets(squareBB, occupancy);
        blockers &= targets;
        return targets ^ this.bishopTargets(squareBB, occupancy ^ blockers);
    }

    private long attackersTo(final long squareBB, final long occupied)
    {
        return (this.singleKingTargets(squareBB) & (this.BK() | this.WK()))
               | (this.knightTargets(squareBB) & (this.BN() | this.WN()))
               | (this.bishopTargets(squareBB, occupied) & (this.BQ() | this.WQ() | this.BB() | this.WB()))
               | (this.rookTargets(squareBB, occupied) & (this.BQ() | this.WQ() | this.BR() | this.WR()))
               | ((this.whitePawnsEastAttackTargets(squareBB) | this.whitePawnsWestAttackTargets(squareBB)) & (this.BP()))
               | ((this.blackPawnsEastAttackTargets(squareBB) | this.blackPawnsWestAttackTargets(squareBB)) & (this.WP()));
    }

    private long knightTargets(long knights)
    {
        long targets = 0;
        while (knights != 0)
        {
            final int ls1b = BitScan.ls1b(knights);
            knights &= ~this.leftShift(1L, ls1b);
            targets |= Knight.targets(ls1b);
        }

        return targets;
    }

    private long singleKingTargets(final long king)
    {
        return King.targets(BitScan.ls1b(king));
    }

    private long kingTargets(long kings)
    {
        long targets = 0;
        while (kings != 0)
        {
            final int ls1b = BitScan.ls1b(kings);
            kings &= ~this.leftShift(1L, ls1b);
            targets |= King.targets(ls1b);
        }
        return targets;
    }

    private long rookTargets(long rooks, final long occupied)
    {
        long attacks = 0;
        while (rooks != 0)
        {
            final int ls1b = BitScan.ls1b(rooks);
            rooks &= (rooks - 1);
            attacks |= MagicCache.getInstance().rookAttacks(ls1b, occupied);
        }
        return attacks;
    }

    private long bishopTargets(long bishops, final long occupied)
    {
        long attacks = 0;
        while (bishops != 0)
        {
            final int ls1b = BitScan.ls1b(bishops);
            bishops &= (bishops - 1);
            attacks |= MagicCache.getInstance().bishopAttacks(ls1b, occupied);
        }
        return attacks;
    }

    private long queenTargets(final long queens, final long occupied)
    {
        return this.bishopTargets(queens, occupied) | this.rookTargets(queens, occupied);
    }

    private long whitePawnSinglePushTargets(final long whitePawns, final long empty)
    {
        return this.northOne(whitePawns) & empty;
    }

    private long blackPawnSinglePushTargets(final long blackPawns, final long empty)
    {
        return this.southOne(blackPawns) & empty;
    }

    private long whitePawnDoublePushTargets(final long whitePawns, final long empty)
    {
        final long singlePush = this.whitePawnSinglePushTargets(whitePawns, empty);
        return this.whitePawnSinglePushTargets(singlePush, empty) & BB_RANK_4;
    }

    private long blackPawnDoublePushTargets(final long blackPawns, final long empty)
    {
        final long singlePush = this.blackPawnSinglePushTargets(blackPawns, empty);
        return this.blackPawnSinglePushTargets(singlePush, empty) & BB_RANK_5;
    }

    private long whitePawnsEastAttackTargets(final long whitePawns)
    {
        return this.NE1(whitePawns);
    }

    private long whitePawnsWestAttackTargets(final long whitePawns)
    {
        return this.NW1(whitePawns);
    }

    private long blackPawnsEastAttackTargets(final long blackPawns)
    {
        return this.SE1(blackPawns);
    }

    private long blackPawnsWestAttackTargets(final long blackPawns)
    {
        return this.SW1(blackPawns);
    }

    private void pseudoWhiteMoves(final long opponentPieces, final long occupied)
    {

        this.pseudoBishopMoves(this.WB(), opponentPieces, occupied);
        this.pseudoRookMoves(this.WR(), opponentPieces, occupied);
        this.pseudoQueenMoves(this.WQ(), opponentPieces, occupied);
        this.pseudoKingMoves(this.WK(), opponentPieces, occupied);
        this.pseudoKnightMoves(this.WN(), opponentPieces, occupied);
        this.pseudoWhitePawnMoves(this.WP(), opponentPieces, occupied);
        this.pseudoWhiteCastles(occupied);
    }

    private void pseudoBlackMoves(final long opponentPieces, final long occupied)
    {
        this.pseudoBishopMoves(this.BB(), opponentPieces, occupied);
        this.pseudoRookMoves(this.BR(), opponentPieces, occupied);
        this.pseudoQueenMoves(this.BQ(), opponentPieces, occupied);
        this.pseudoKingMoves(this.BK(), opponentPieces, occupied);
        this.pseudoKnightMoves(this.BN(), opponentPieces, occupied);
        this.pseudoBlackPawnMoves(this.BP(), opponentPieces, occupied);
        this.pseudoBlackCastles(occupied);
    }

    private void legalWhiteMoves(final long opponentPieces, final long occupied)
    {
        final long ownPieces = this.whitePieces();
        final long myKing = this.pieceList.piecesBB(Side.WHITE, PieceType.KING);
        final int kingSq = BitScan.ls1b(myKing);
        final long kingAttackers = opponentPieces & this.attackersTo(myKing, occupied);
        final int attackersCount = PopulationCount.popCount(kingAttackers);
        final long opponentTargets = this.blackTargets(occupied);
        final long pinners = this.getPinners(occupied, ownPieces, myKing);

        if (attackersCount == 2)
        {
            this.legalKingMoves(this.WK(), opponentPieces, occupied, opponentTargets);
        }
        else
        {
            this.legalWhitePawnMoves(this.WP(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalQueenMoves(this.WQ(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalRookMoves(this.WR(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalBishopMoves(this.WB(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalKnightMoves(this.WN(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalWhiteCastles(occupied, opponentTargets);
            this.legalKingMoves(this.WK(), opponentPieces, occupied, opponentTargets);
        }
    }

    private void legalBlackMoves(final long opponentPieces, final long occupied)
    {
        final long ownPieces = this.blackPieces();
        final long myKing = this.pieceList.piecesBB(Side.BLACK, PieceType.KING);
        final int kingSq = BitScan.ls1b(myKing);
        final long kingAttackers = opponentPieces & this.attackersTo(myKing, occupied);
        final int attackersCount = PopulationCount.popCount(kingAttackers);
        final long opponentTargets = this.whiteTargets(occupied);
        final long pinners = this.getPinners(occupied, ownPieces, myKing);

        if (attackersCount == 2)
        {
            this.legalKingMoves(this.BK(), opponentPieces, occupied, opponentTargets);
        }
        else
        {
            this.legalBlackPawnMoves(this.BP(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalQueenMoves(this.BQ(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalRookMoves(this.BR(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalBishopMoves(this.BB(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalKnightMoves(this.BN(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            this.legalBlackCastles(occupied, opponentTargets);
            this.legalKingMoves(this.BK(), opponentPieces, occupied, opponentTargets);

        }
    }

    private void legalKingMoves(long kingBB, final long opponentPieces, final long occupied, final long opponentAttacks)
    {
        while (kingBB != 0)
        {
            final int fromSq = BitScan.ls1b(kingBB);
            kingBB &= ~this.leftShift(1L, fromSq);
            long attackSet = King.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                final int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                if (!this.intersects(BoardUtil.squareBB(toSquare), opponentAttacks))
                {
                    final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                    this.addMove(Move.move(fromSq, toSquare, moveType));
                }
            }
        }
    }

    private void pseudoKingMoves(long kingBB, final long opponentPieces, final long occupied)
    {
        while (kingBB != 0)
        {
            final int fromSq = BitScan.ls1b(kingBB);
            kingBB &= ~this.leftShift(1L, fromSq);
            long attackSet = King.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                final int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(fromSq, toSquare, moveType));
            }
        }
    }

    private void legalKnightMoves(long knightsBB, final long opponentPieces, final long occupied, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        while (knightsBB != 0)
        {
            final int fromSq = BitScan.ls1b(knightsBB);
            knightsBB &= (knightsBB - 1);
            long attackSet = Knight.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                final int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                if (this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(fromSq), BoardUtil.squareBB(toSquare))
                    || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(fromSq, toSquare, moveType));
            }
        }
    }

    private void pseudoKnightMoves(long knightsBB, final long opponentPieces, final long occupied)
    {
        while (knightsBB != 0)
        {
            final int fromSq = BitScan.ls1b(knightsBB);
            knightsBB &= (knightsBB - 1);
            long attackSet = Knight.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                final int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(fromSq, toSquare, moveType));
            }
        }
    }

    private void legalRookMoves(long rooks, final long opponentPieces, final long occupied, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        while (rooks != 0)
        {
            final int rook = BitScan.ls1b(rooks);
            rooks &= ~this.leftShift(1L, rook);
            long attackSet = MagicCache.getInstance().rookAttacks(rook, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                final int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                if (this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(rook), BoardUtil.squareBB(toSquare))
                    || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(rook, toSquare, moveType));
            }
        }
    }

    private void pseudoRookMoves(long rooks, final long opponentPieces, final long occupied)
    {
        while (rooks != 0)
        {
            final int rook = BitScan.ls1b(rooks);
            rooks &= ~this.leftShift(1L, rook);
            long attackSet = MagicCache.getInstance().rookAttacks(rook, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                final int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(rook, toSquare, moveType));
            }
        }
    }

    private void legalBishopMoves(long bishops, final long opponentPieces, final long occupied, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        while (bishops != 0)
        {
            final int bishop = BitScan.ls1b(bishops);
            bishops &= (bishops - 1);
            long attackSet = MagicCache.getInstance().bishopAttacks(bishop, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                final int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                if (this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(bishop), BoardUtil.squareBB(toSquare))
                    || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(bishop, toSquare, moveType));
            }
        }
    }

    private void pseudoBishopMoves(long bishops, final long opponentPieces, final long occupied)
    {
        while (bishops != 0)
        {
            final int bishop = BitScan.ls1b(bishops);
            bishops &= (bishops - 1);
            long attackSet = MagicCache.getInstance().bishopAttacks(bishop, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                final int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(bishop, toSquare, moveType));
            }
        }
    }

    private void legalQueenMoves(long queens, final long opponentPieces, final long occupied, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        while (queens != 0)
        {
            final int queen = BitScan.ls1b(queens);
            queens &= (queens - 1);

            // rook attacks
            long rookAttackSets = MagicCache.getInstance().rookAttacks(queen, occupied);
            rookAttackSets &= ~occupied | opponentPieces;
            while (rookAttackSets != 0)
            {
                final int toSquare = BitScan.ls1b(rookAttackSets);
                rookAttackSets &= ~BoardUtil.squareBB(toSquare);
                if (this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(queen), BoardUtil.squareBB(toSquare))
                    || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(queen, toSquare, moveType));
            }

            // bishop attacks
            long bishopAttackSets = MagicCache.getInstance().bishopAttacks(queen, occupied);
            bishopAttackSets &= ~occupied | opponentPieces;
            while (bishopAttackSets != 0)
            {
                final int toSquare = BitScan.ls1b(bishopAttackSets);
                bishopAttackSets &= ~BoardUtil.squareBB(toSquare);
                if (this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(queen), BoardUtil.squareBB(toSquare))
                    || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }

                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(queen, toSquare, moveType));
            }
        }
    }

    private void pseudoQueenMoves(long queens, final long opponentPieces, final long occupied)
    {
        while (queens != 0)
        {
            final int queen = BitScan.ls1b(queens);
            queens &= (queens - 1);

            // rook attacks
            long rookAttackSets = MagicCache.getInstance().rookAttacks(queen, occupied);
            rookAttackSets &= ~occupied | opponentPieces;
            while (rookAttackSets != 0)
            {
                final int toSquare = BitScan.ls1b(rookAttackSets);
                rookAttackSets &= ~BoardUtil.squareBB(toSquare);
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(queen, toSquare, moveType));
            }

            // bishop attacks
            long bishopAttackSets = MagicCache.getInstance().bishopAttacks(queen, occupied);
            bishopAttackSets &= ~occupied | opponentPieces;
            while (bishopAttackSets != 0)
            {
                final int toSquare = BitScan.ls1b(bishopAttackSets);
                bishopAttackSets &= ~BoardUtil.squareBB(toSquare);
                final short moveType = this.intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                this.addMove(Move.move(queen, toSquare, moveType));
            }
        }
    }

    private void legalWhiteCastles(final long occupied, final long opponentAttacks)
    {
        if (this.intersects(this.WK(), opponentAttacks))
        {
            return;
        }

        if (((W_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (W_KING_CASTLE_ATTACK_MASK & opponentAttacks) == 0
            && (this.WK() & BB_E1) != 0
            && (this.WR() & BB_H1) != 0
            && this.canKingSideCastleWhite())
        {
            this.addMove(Move.move(SQ_E1, SQ_G1, Move.KING_CASTLE));
        }

        if ((W_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (W_QUEEN_CASTLE_ATTACK_MASK & opponentAttacks) == 0
            && (this.WK() & BB_E1) != 0
            && (this.WR() & BB_A1) != 0
            && this.canQueenSideCastleWhite())
        {
            this.addMove(Move.move(SQ_E1, SQ_C1, Move.QUEEN_CASTLE));
        }
    }

    public boolean canQueenSideCastleWhite()
    {
        return this.canCastle(WQ_CASTLE_MASK);
    }

    public boolean canKingSideCastleWhite()
    {
        return this.canCastle(WK_CASTLE_MASK);
    }

    private void legalBlackCastles(final long occupied, final long opponentAttacks)
    {
        if (this.intersects(this.BK(), opponentAttacks))
        {
            return;
        }

        if (((B_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (B_KING_CASTLE_ATTACK_MASK & opponentAttacks) == 0
            && (this.BK() & BB_E8) != 0
            && (this.BR() & BB_H8) != 0
            && this.canKingSideCastleBlack())
        {
            this.addMove(Move.move(SQ_E8, SQ_G8, Move.KING_CASTLE));
        }

        if ((B_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (B_QUEEN_CASTLE_ATTACK_MASK & opponentAttacks) == 0
            && (this.BK() & BB_E8) != 0
            && (this.BR() & BB_A8) != 0
            && this.canQueenSideCastleBlack())
        {
            this.addMove(Move.move(SQ_E8, SQ_C8, Move.QUEEN_CASTLE));
        }
    }

    public boolean canQueenSideCastleBlack()
    {
        return this.canCastle(BQ_CASTLE_MASK);
    }

    private void pseudoWhiteCastles(final long occupied)
    {
        if (((W_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (this.WK() & BB_E1) != 0
            && (this.WR() & BB_H1) != 0
            && this.canKingSideCastleWhite())
        {
            this.addMove(Move.move(SQ_E1, SQ_G1, Move.KING_CASTLE));
        }

        if ((W_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (this.WK() & BB_E1) != 0
            && (this.WR() & BB_A1) != 0
            && this.canQueenSideCastleWhite())
        {
            this.addMove(Move.move(SQ_E1, SQ_C1, Move.QUEEN_CASTLE));
        }
    }

    private void pseudoBlackCastles(final long occupied)
    {
        if (((B_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (this.BK() & BB_E8) != 0
            && (this.BR() & BB_H8) != 0
            && this.canKingSideCastleBlack())
        {
            this.addMove(Move.move(SQ_E8, SQ_G8, Move.KING_CASTLE));
        }

        if ((B_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (this.BK() & BB_E8) != 0
            && (this.BR() & BB_A8) != 0
            && this.canQueenSideCastleBlack())
        {
            this.addMove(Move.move(SQ_E8, SQ_C8, Move.QUEEN_CASTLE));
        }
    }

    public boolean canKingSideCastleBlack()
    {
        return this.canCastle(BK_CASTLE_MASK);
    }

    private void legalWhitePawnMoves(final long whitePawns, final long opponentPieces, final long occupied, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        if (whitePawns == 0)
        {
            return;
        }

        final long empty = ~occupied;

        final long singlePushes = this.whitePawnSinglePushTargets(whitePawns, empty) & ~BB_RANK_8;
        final long doublePushes = this.whitePawnDoublePushTargets(whitePawns, empty);
        final long attacksNE = this.whitePawnsEastAttackTargets(whitePawns) & opponentPieces & ~BB_RANK_8;
        final long attacksNW = this.whitePawnsWestAttackTargets(whitePawns) & opponentPieces & ~BB_RANK_8;

        final long promotionsPush = this.whitePawnSinglePushTargets(whitePawns, empty) & BB_RANK_8;
        final long promotionAttacksNE = this.whitePawnsEastAttackTargets(whitePawns) & opponentPieces & BB_RANK_8;
        final long promotionAttacksNW = this.whitePawnsWestAttackTargets(whitePawns) & opponentPieces & BB_RANK_8;

        this.addLegalPawnMoves(singlePushes, Direction.NORTH, Move.QUIET_MOVE, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnMoves(doublePushes, Direction.NORTH * 2, Move.DOUBLE_PAWN_PUSH, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnMoves(attacksNE, Direction.NORTH_EAST, Move.CAPTURES, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnMoves(attacksNW, Direction.NORTH_WEST, Move.CAPTURES, kingSq, kingAttackers, kingAttackersCount, pinners);

        this.addLegalPawnPromotions(promotionsPush, Direction.NORTH, false, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnPromotions(promotionAttacksNE, Direction.NORTH_EAST, true, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnPromotions(promotionAttacksNW, Direction.NORTH_WEST, true, kingSq, kingAttackers, kingAttackersCount, pinners);

        this.addLegalEnPassantForWhite(occupied, kingSq, kingAttackers, kingAttackersCount, pinners);
    }

    private void pseudoWhitePawnMoves(final long whitePawns, final long opponentPieces, final long occupied)
    {
        if (whitePawns == 0)
        {
            return;
        }

        final long empty = ~occupied;

        final long singlePushes = this.whitePawnSinglePushTargets(whitePawns, empty) & ~BB_RANK_8;
        final long doublePushes = this.whitePawnDoublePushTargets(whitePawns, empty);
        final long attacksNE = this.whitePawnsEastAttackTargets(whitePawns) & opponentPieces & ~BB_RANK_8;
        final long attacksNW = this.whitePawnsWestAttackTargets(whitePawns) & opponentPieces & ~BB_RANK_8;

        final long promotionsPush = this.whitePawnSinglePushTargets(whitePawns, empty) & BB_RANK_8;
        final long promotionAttacksNE = this.whitePawnsEastAttackTargets(whitePawns) & opponentPieces & BB_RANK_8;
        final long promotionAttacksNW = this.whitePawnsWestAttackTargets(whitePawns) & opponentPieces & BB_RANK_8;

        this.addPawnMoves(singlePushes, Direction.NORTH, Move.QUIET_MOVE);
        this.addPawnMoves(doublePushes, Direction.NORTH * 2, Move.DOUBLE_PAWN_PUSH);
        this.addPawnMoves(attacksNE, Direction.NORTH_EAST, Move.CAPTURES);
        this.addPawnMoves(attacksNW, Direction.NORTH_WEST, Move.CAPTURES);

        this.addPawnPromotions(promotionsPush, Direction.NORTH, false);
        this.addPawnPromotions(promotionAttacksNE, Direction.NORTH_EAST, true);
        this.addPawnPromotions(promotionAttacksNW, Direction.NORTH_WEST, true);

        this.addEnPassantForWhite(occupied);
    }

    private void legalBlackPawnMoves(final long blackPawns, final long opponentPieces, final long occupied, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        if (blackPawns == 0)
        {
            return;
        }

        final long empty = ~occupied;

        final long singlePushes = this.blackPawnSinglePushTargets(blackPawns, empty) & ~BB_RANK_1;
        final long doublePushes = this.blackPawnDoublePushTargets(blackPawns, empty);
        final long attacksSE = this.blackPawnsEastAttackTargets(blackPawns) & opponentPieces & ~BB_RANK_1;
        final long attacksSW = this.blackPawnsWestAttackTargets(blackPawns) & opponentPieces & ~BB_RANK_1;

        final long promotionsPush = this.blackPawnSinglePushTargets(blackPawns, empty) & BB_RANK_1;
        final long promotionAttacksSE = this.blackPawnsEastAttackTargets(blackPawns) & opponentPieces & BB_RANK_1;
        final long promotionAttacksSW = this.blackPawnsWestAttackTargets(blackPawns) & opponentPieces & BB_RANK_1;

        this.addLegalPawnMoves(singlePushes, Direction.SOUTH, Move.QUIET_MOVE, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnMoves(doublePushes, Direction.SOUTH * 2, Move.DOUBLE_PAWN_PUSH, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnMoves(attacksSE, Direction.SOUTH_EAST, Move.CAPTURES, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnMoves(attacksSW, Direction.SOUTH_WEST, Move.CAPTURES, kingSq, kingAttackers, kingAttackersCount, pinners);

        this.addLegalPawnPromotions(promotionsPush, Direction.SOUTH, false, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnPromotions(promotionAttacksSE, Direction.SOUTH_EAST, true, kingSq, kingAttackers, kingAttackersCount, pinners);
        this.addLegalPawnPromotions(promotionAttacksSW, Direction.SOUTH_WEST, true, kingSq, kingAttackers, kingAttackersCount, pinners);

        this.addEnPassantForBlack(occupied, kingSq, kingAttackers, kingAttackersCount, pinners);
    }

    private void pseudoBlackPawnMoves(final long blackPawns, final long opponentPieces, final long occupied)
    {
        if (blackPawns == 0)
        {
            return;
        }

        final long empty = ~occupied;

        final long singlePushes = this.blackPawnSinglePushTargets(blackPawns, empty) & ~BB_RANK_1;
        final long doublePushes = this.blackPawnDoublePushTargets(blackPawns, empty);
        final long attacksSE = this.blackPawnsEastAttackTargets(blackPawns) & opponentPieces & ~BB_RANK_1;
        final long attacksSW = this.blackPawnsWestAttackTargets(blackPawns) & opponentPieces & ~BB_RANK_1;

        final long promotionsPush = this.blackPawnSinglePushTargets(blackPawns, empty) & BB_RANK_1;
        final long promotionAttacksSE = this.blackPawnsEastAttackTargets(blackPawns) & opponentPieces & BB_RANK_1;
        final long promotionAttacksSW = this.blackPawnsWestAttackTargets(blackPawns) & opponentPieces & BB_RANK_1;

        this.addPawnMoves(singlePushes, Direction.SOUTH, Move.QUIET_MOVE);
        this.addPawnMoves(doublePushes, Direction.SOUTH * 2, Move.DOUBLE_PAWN_PUSH);
        this.addPawnMoves(attacksSE, Direction.SOUTH_EAST, Move.CAPTURES);
        this.addPawnMoves(attacksSW, Direction.SOUTH_WEST, Move.CAPTURES);

        this.addPawnPromotions(promotionsPush, Direction.SOUTH, false);
        this.addPawnPromotions(promotionAttacksSE, Direction.SOUTH_EAST, true);
        this.addPawnPromotions(promotionAttacksSW, Direction.SOUTH_WEST, true);

        this.addEnPassantForBlack(occupied);
    }

    private void addPawnPromotions(long targetSquares, final int shift, final boolean isCapture)
    {
        while (targetSquares != 0)
        {
            final int toSquare = BitScan.ls1b(targetSquares);
            final int fromSquare = toSquare - shift;
            targetSquares &= ~BoardUtil.squareBB(toSquare);

            if (isCapture)
            {
                this.addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMO_CAPTURE));
                this.addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMO_CAPTURE));
                this.addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMO_CAPTURE));
                this.addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMO_CAPTURE));
            }
            else
            {
                this.addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMOTION));
                this.addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMOTION));
                this.addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMOTION));
                this.addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMOTION));
            }
        }
    }

    private void addLegalPawnPromotions(long targetSquares, final int shift, final boolean isCapture, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        while (targetSquares != 0)
        {
            final int toSquare = BitScan.ls1b(targetSquares);
            final int fromSquare = toSquare - shift;
            targetSquares &= ~BoardUtil.squareBB(toSquare);

            if (this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(fromSquare), BoardUtil.squareBB(toSquare))
                || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
            {
                continue;
            }

            if (isCapture)
            {
                this.addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMO_CAPTURE));
                this.addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMO_CAPTURE));
                this.addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMO_CAPTURE));
                this.addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMO_CAPTURE));
            }
            else
            {
                this.addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMOTION));
                this.addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMOTION));
                this.addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMOTION));
                this.addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMOTION));
            }
        }
    }

    private void addLegalEnPassantForWhite(final long occupied, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        final int epFileIndex = this.getEnPassantFile(this.irreversibleState) - 1;
        if (epFileIndex < 0)
        {
            return;
        }

        final long targetPawnBB = BoardUtil.squareBB(epFileIndex, RANK_5);
        final long targetSquareBB = BoardUtil.squareBB(epFileIndex, RANK_6);
        final int targetSquare = BoardUtil.square(epFileIndex, RANK_6);

        if (!this.intersects(targetPawnBB, this.BP()) || this.intersects(targetSquareBB, occupied))
        {
            return;
        }

        if (this.intersects(this.eastOne(this.WP()), targetPawnBB))
        {
            if (!(this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(targetSquare + Direction.SOUTH_WEST), BoardUtil.squareBB(targetSquare))
                  || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(targetSquare), kingAttackersCount))
                || kingAttackers == targetPawnBB) // single pawn attacker, which can be removed
            {
                this.addMove(Move.move(targetSquare + Direction.SOUTH_WEST, targetSquare, Move.EP_CAPTURE));
            }
        }

        if (this.intersects(this.westOne(this.WP()), targetPawnBB))
        {
            if (!(this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(targetSquare + Direction.SOUTH_EAST), BoardUtil.squareBB(targetSquare))
                  || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(targetSquare), kingAttackersCount))
                || kingAttackers == targetPawnBB) // single pawn attacker, which can be removed
            {
                this.addMove(Move.move(targetSquare + Direction.SOUTH_EAST, targetSquare, Move.EP_CAPTURE));

            }
        }
    }

    private void addEnPassantForWhite(final long occupied)
    {
        final int epFileIndex = this.getEnPassantFile(this.irreversibleState) - 1;
        if (epFileIndex < 0)
        {
            return;
        }

        final long targetPawnBB = BoardUtil.squareBB(epFileIndex, RANK_5);
        final long targetSquareBB = BoardUtil.squareBB(epFileIndex, RANK_6);
        final int targetSquare = BoardUtil.square(epFileIndex, RANK_6);

        if (!this.intersects(targetPawnBB, this.BP()) || this.intersects(targetSquareBB, occupied))
        {
            return;
        }

        if (this.intersects(this.eastOne(this.WP()), targetPawnBB))
        {
            this.addMove(Move.move(targetSquare + Direction.SOUTH_WEST, targetSquare, Move.EP_CAPTURE));
        }

        if (this.intersects(this.westOne(this.WP()), targetPawnBB))
        {
            this.addMove(Move.move(targetSquare + Direction.SOUTH_EAST, targetSquare, Move.EP_CAPTURE));
        }
    }

    private void addEnPassantForBlack(final long occupied, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        final int epFileIndex = this.getEnPassantFile(this.irreversibleState) - 1;
        if (epFileIndex < 0)
        {
            return;
        }

        final long targetPawnBB = BoardUtil.squareBB(epFileIndex, RANK_4);
        final long targetSquareBB = BoardUtil.squareBB(epFileIndex, RANK_3);
        final int targetSquare = BoardUtil.square(epFileIndex, RANK_3);

        if (!this.intersects(targetPawnBB, this.WP()) || this.intersects(targetSquareBB, occupied))
        {
            return;
        }

        if (this.intersects(this.eastOne(this.BP()), targetPawnBB))
        {
            if (!(this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(targetSquare + Direction.NORTH_WEST), BoardUtil.squareBB(targetSquare))
                  || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(targetSquare), kingAttackersCount))
                || kingAttackers == targetPawnBB) // single pawn attacker, which can be removed
            {
                this.addMove(Move.move(targetSquare + Direction.NORTH_WEST, targetSquare, Move.EP_CAPTURE));
            }
        }

        if (this.intersects(this.westOne(this.BP()), targetPawnBB))
        {
            if (!(this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(targetSquare + Direction.NORTH_EAST), BoardUtil.squareBB(targetSquare))
                  || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(targetSquare), kingAttackersCount))
                || kingAttackers == targetPawnBB) // single pawn attacker, which can be removed
            {
                this.addMove(Move.move(targetSquare + Direction.NORTH_EAST, targetSquare, Move.EP_CAPTURE));
            }
        }
    }

    private void addEnPassantForBlack(final long occupied)
    {
        final int epFileIndex = this.getEnPassantFile(this.irreversibleState) - 1;
        if (epFileIndex < 0)
        {
            return;
        }

        final long targetPawnBB = BoardUtil.squareBB(epFileIndex, RANK_4);
        final long targetSquareBB = BoardUtil.squareBB(epFileIndex, RANK_3);
        final int targetSquare = BoardUtil.square(epFileIndex, RANK_3);

        if (!this.intersects(targetPawnBB, this.WP()) || this.intersects(targetSquareBB, occupied))
        {
            return;
        }

        if (this.intersects(this.eastOne(this.BP()), targetPawnBB))
        {
            this.addMove(Move.move(targetSquare + Direction.NORTH_WEST, targetSquare, Move.EP_CAPTURE));
        }

        if (this.intersects(this.westOne(this.BP()), targetPawnBB))
        {
            this.addMove(Move.move(targetSquare + Direction.NORTH_EAST, targetSquare, Move.EP_CAPTURE));
        }
    }

    private void addLegalPawnMoves(long targetSquares, final int shift, final short moveType, final int kingSq, final long kingAttackers, final int kingAttackersCount, final long pinners)
    {
        while (targetSquares != 0)
        {
            final int toSquare = BitScan.ls1b(targetSquares);
            final int fromSquare = toSquare - shift;
            targetSquares &= ~BoardUtil.squareBB(toSquare);

            if (this.isMovePinned(kingSq, pinners, BoardUtil.squareBB(fromSquare), BoardUtil.squareBB(toSquare))
                || !this.canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
            {
                continue;
            }

            if (Move.isPromotion(moveType))
            {
                if (Move.isCapture(moveType))
                {
                    this.addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMO_CAPTURE));
                    this.addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMO_CAPTURE));
                    this.addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMO_CAPTURE));
                    this.addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMO_CAPTURE));
                }
                else
                {
                    this.addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMOTION));
                    this.addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMOTION));
                    this.addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMOTION));
                    this.addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMOTION));
                }
            }
            else
            {
                this.addMove(Move.move(fromSquare, toSquare, moveType));
            }
        }
    }

    private void addPawnMoves(long targetSquares, final int shift, final short moveType)
    {
        while (targetSquares != 0)
        {
            final int toSquare = BitScan.ls1b(targetSquares);
            final int fromSquare = toSquare - shift;
            targetSquares &= ~BoardUtil.squareBB(toSquare);

            if (Move.isPromotion(moveType))
            {
                if (Move.isCapture(moveType))
                {
                    this.addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMO_CAPTURE));
                    this.addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMO_CAPTURE));
                    this.addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMO_CAPTURE));
                    this.addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMO_CAPTURE));
                }
                else
                {
                    this.addMove(Move.move(fromSquare, toSquare, Move.KNIGHT_PROMOTION));
                    this.addMove(Move.move(fromSquare, toSquare, Move.BISHOP_PROMOTION));
                    this.addMove(Move.move(fromSquare, toSquare, Move.ROOK_PROMOTION));
                    this.addMove(Move.move(fromSquare, toSquare, Move.QUEEN_PROMOTION));
                }
            }
            else
            {
                this.addMove(Move.move(fromSquare, toSquare, moveType));
            }
        }
    }

    private long leftShift(final long x, final int s)
    {
        return x << s;
    }

    private long rightShift(final long x, final int s)
    {
        return x >>> s;
    }

    private long northOne(final long bb)
    {
        return this.leftShift(bb, BOARD_DIM);
    }

    private long southOne(final long bb)
    {
        return this.rightShift(bb, BOARD_DIM);
    }

    private long eastOne(final long bb)
    {
        return this.leftShift(bb, 1) & ~BB_FILE_A;
    }

    private long westOne(final long bb)
    {
        return this.rightShift(bb, 1) & ~BB_FILE_H;
    }

    private long NE1(final long bb)
    {
        return this.leftShift(bb, 9) & ~BB_FILE_A;
    }

    private long NW1(final long bb)
    {
        return this.leftShift(bb, 7) & ~BB_FILE_H;
    }

    private long SE1(final long bb)
    {
        return this.rightShift(bb, 7) & ~BB_FILE_A;
    }

    private long SW1(final long bb)
    {
        return this.rightShift(bb, 9) & ~BB_FILE_H;
    }

    private boolean intersects(final long a, final long b)
    {
        return (a & b) != 0;
    }

    private boolean canCastle(final short castleMask)
    {
        return (this.getCastleRights(this.irreversibleState) & castleMask) != 0;
    }

    private short setCastleBit(short castleRights, final short castleMask)
    {
        castleRights |= castleMask;
        return castleRights;
    }

    private short unsetCastleBit(short castleRights, final short castleMask)
    {
        return castleRights &= ~castleMask;
    }

    private long WK()
    {
        return this.pieceList.piecesBB(Piece.W_KING);
    }

    private long WQ()
    {
        return this.pieceList.piecesBB(Piece.W_QUEEN);
    }

    private long WR()
    {
        return this.pieceList.piecesBB(Piece.W_ROOK);
    }

    private long WB()
    {
        return this.pieceList.piecesBB(Piece.W_BISHOP);
    }

    private long WN()
    {
        return this.pieceList.piecesBB(Piece.W_KNIGHT);
    }

    private long WP()
    {
        return this.pieceList.piecesBB(Piece.W_PAWN);
    }

    private long BK()
    {
        return this.pieceList.piecesBB(Piece.B_KING);
    }

    private long BQ()
    {
        return this.pieceList.piecesBB(Piece.B_QUEEN);
    }

    private long BR()
    {
        return this.pieceList.piecesBB(Piece.B_ROOK);
    }

    private long BB()
    {
        return this.pieceList.piecesBB(Piece.B_BISHOP);
    }

    private long BN()
    {
        return this.pieceList.piecesBB(Piece.B_KNIGHT);
    }

    private long BP()
    {
        return this.pieceList.piecesBB(Piece.B_PAWN);
    }

    private void addMove(final short move)
    {
        this.potentialMoves[this.moveCount++] = move;
    }

    public void debug()
    {
        System.out.println("state: " + this.irreversibleState);
        System.out.println("castle rights: " + this.getCastleRights(this.irreversibleState));
    }

    public int identify()
    {
        return this.pieceList.hashCode();
    }

    public PieceList getPieceList()
    {
        return this.pieceList;
    }
}
