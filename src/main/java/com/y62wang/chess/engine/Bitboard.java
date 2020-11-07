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
    public static int MAX_MOVES_PER_POSITION = 256;
    public static int MAX_MOVES_PER_GAME = 10000;
    public static long MAKE_MOVE_TIME = 0;
    public static long LEGAL_MOVE_TIME = 0;

    public static int BOARD_WIDTH = 8;
    public static int SIZE = 64;

    private static short WK_CASTLE_MASK = 1;
    private static short WQ_CASTLE_MASK = 2;
    private static short BK_CASTLE_MASK = 4;
    private static short BQ_CASTLE_MASK = 8;
    private static short FULL_CASTLE_RIGHT = (1 << 4) - 1;
    public static int NO_EP_TARGET = 0;

    private static int[] historyStates = new int[MAX_MOVES_PER_GAME];
    private static int stateCount = 0;

    private short fullMoveNumber;
    private short halfMoveClock;
    private Side turn;
    private PieceList pieceList;
    private short[] potentialMoves;
    private int moveCount;

    // history structure: [3 bits: captured piece] [16 bits: move] [4 bits: EP] [4 bits castle]
    private int irreversibleState;

    public Bitboard()
    {
        this(CharacterUtilities.toLittleEndianBoard(NEW_BOARD_CHARS));
    }

    private Bitboard(char[] board)
    {
        assignPiece(board);
        turn = Side.WHITE;
        irreversibleState = getIrreversibleState(FULL_CASTLE_RIGHT, NO_EP_TARGET, 0);
        potentialMoves = new short[MAX_MOVES_PER_POSITION];
    }

    public Bitboard(Bitboard bb)
    {
        pieceList = bb.pieceList.copy();
        turn = bb.turn;
        irreversibleState = bb.irreversibleState;
        potentialMoves = new short[MAX_MOVES_PER_POSITION];
        moveCount = 0;
    }

    public Bitboard(String FEN)
    {
        parseFEN(FEN);
    }

    private void parseFEN(final String FEN)
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

        short castleRights = 0;
        castleRights |= tokens[2].contains("K") ? 1 : 0;
        castleRights |= tokens[2].contains("Q") ? (1 << 1) : 0;
        castleRights |= tokens[2].contains("k") ? (1 << 2) : 0;
        castleRights |= tokens[2].contains("q") ? (1 << 3) : 0;
        int epFile = tokens[3].equals("-") ? 0 : Integer.parseInt(tokens[3]);

        irreversibleState = getIrreversibleState(castleRights, epFile, 0);

        halfMoveClock = tokens.length > 4 ? Short.parseShort(tokens[4]) : 0;
        fullMoveNumber = tokens.length > 5 ? Short.parseShort(tokens[5]) : 0;

        potentialMoves = new short[MAX_MOVES_PER_POSITION];
    }

    public Side getTurn()
    {
        return turn;
    }

    private int getIrreversibleState(int castleRights, int epFile, int capturedPiece)
    {
        return castleRights | epFile << 4 | capturedPiece << 24;
    }

    private int getIrreversibleState(int castleRights, int epFile, int capturedPiece, short move)
    {
        return castleRights | epFile << 4 | (Short.toUnsignedInt(move)) << 8 | capturedPiece << 24;
    }

    public int getCastleRights()
    {
        return getCastleRights(irreversibleState);
    }

    public int getEPFile()
    {
        return getEnPassantFile(irreversibleState);
    }

    private short getCastleRights(int irreversibleState)
    {
        return ( short ) (FULL_CASTLE_RIGHT & irreversibleState);
    }

    private int getEnPassantFile(int irreversibleState)
    {
        return (irreversibleState >>> 4) & ((1 << 4) - 1);
    }

    private short getMove(int irreversibleState)
    {
        return ( short ) ((irreversibleState >>> 8) & ((1 << 16) - 1));
    }

    private PieceType getCapturedPiece(int irreversibleState)
    {
        int pieceIndex = (irreversibleState >>> 24) & ((1 << 3) - 1);
        return PieceType.of(pieceIndex);
    }

    private void assignPiece(char[] board)
    {
        pieceList = new PieceList();

        if (board.length != SIZE)
        {
            throw new RuntimeException("invalid board size: " + board.length);
        }

        for (int i = 0; i < board.length; i++)
        {
            char pieceChar = board[i];
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
        return pieceList.occupied();
    }

    private long whitePieces()
    {
        return WP() | WN() | WB() | WR() | WK() | WQ();
    }

    private long blackPieces()
    {
        return BP() | BN() | BB() | BR() | BK() | BQ();
    }

    public long targets(Side side)
    {
        return side == Side.WHITE ? whiteTargets(occupied()) : blackTargets(occupied());
    }

    public long pieces(Side side)
    {
        return pieceList.sideBB(side);
    }

    private long whiteTargets(long occupied)
    {
        long targets = singleKingTargets(WK())
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
        long targets = singleKingTargets(BK())
                       | queenTargets(BQ(), occupied & ~WK())
                       | knightTargets(BN())
                       | rookTargets(BR(), occupied & ~WK())
                       | bishopTargets(BB(), occupied & ~WK())
                       | blackPawnsEastAttackTargets(BP())
                       | blackPawnsWestAttackTargets(BP());
        return targets;
    }

    public void unmake()
    {
        try
        {
            unmakeMove();
        }
        catch (Exception ex)
        {
            log.error("failing to unmake move" + toString(), ex);
            throw ex;
        }
    }

    public void unmakeMove()
    {
        int irreversibleState = historyStates[--stateCount];
        short move = getMove(irreversibleState);

        int fromSq = Move.fromSquare(move);
        int toSq = Move.toSquare(move);
        pieceList.movePiece(toSq, fromSq);

        int moveCode = Move.moveCode(move);
        if (moveCode == Move.QUIET_MOVE || moveCode == Move.DOUBLE_PAWN_PUSH)
        {
        }
        else if (moveCode == Move.CAPTURES)
        {
            pieceList.addPiece(Piece.of(turn, getCapturedPiece(irreversibleState)), toSq);
        }
        else if (Move.isPromoCapture(move))
        {
            pieceList.removePiece(fromSq);
            pieceList.addPiece(Piece.of(turn.flip(), PieceType.PAWN), fromSq);
            pieceList.addPiece(Piece.of(turn, getCapturedPiece(irreversibleState)), toSq);
        }
        else if (Move.isPromotion(move))
        {
            pieceList.removePiece(fromSq);
            pieceList.addPiece(Piece.of(turn.flip(), PieceType.PAWN), fromSq);
        }
        else if (Move.isKingCastle(move))
        {
            if (turn == Side.BLACK)
            {
                pieceList.movePiece(SQ_F1, SQ_H1);
            }
            else
            {
                pieceList.movePiece(SQ_F8, SQ_H8);
            }
        }
        else if (Move.isQueenCastle(move))
        {
            if (turn == Side.BLACK)
            {
                pieceList.movePiece(SQ_D1, SQ_A1);
            }
            else
            {
                pieceList.movePiece(SQ_D8, SQ_A8);
            }
        }
        else if (Move.isEnpassant(move))
        {
            int moveDirection = turn == Side.BLACK ? Direction.SOUTH : Direction.NORTH;
            pieceList.addPiece(Piece.of(turn, PieceType.PAWN), toSq + moveDirection);
        }

        turn = turn.flip();
        this.irreversibleState = irreversibleState;
    }

    public void makeMove(String move)
    {
        try
        {
            makeMove(Move.of(move));
        }
        catch (Exception ex)
        {
            log.error("failing to make move", ex);
            throw ex;
        }
    }

    public void makeMove(short move)
    {
        // Stopwatch started = Stopwatch.createStarted();
        long from = Move.fromSquareBB(move);
        int fromSquare = Move.fromSquare(move);
        int toSquare = Move.toSquare(move);

        assert (intersects(from, occupied()));

        short updatedCastleRights = getUpdatedCastleRights(from);

        PieceList copy = pieceList;
        Piece capturedPiece = pieceList.onSquare(toSquare);
        historyStates[stateCount++] = getIrreversibleState(getCastleRights(irreversibleState),
                                                           getEnPassantFile(irreversibleState),
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
            newEnPassantTarget = BoardUtil.file(Move.toSquare(move)) + 1;
        }
        turn = nextTurn();
        pieceList = copy;
        irreversibleState = getIrreversibleState(updatedCastleRights, newEnPassantTarget, 0, ( short ) 0);

    }

    private short getUpdatedCastleRights(long from)
    {
        short updatedCastleRights = getCastleRights(irreversibleState);

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
        legalMoves2();
        return Arrays.copyOf(potentialMoves, moveCount);
    }

    private void legalMoves2()
    {
        long occupied = occupied();
        long ownPieces = turn == Side.WHITE ? whitePieces() : blackPieces();
        long opponentPieces = occupied & ~ownPieces;
        if (turn == Side.WHITE)
        {
            legalWhiteMoves(opponentPieces, occupied);
        }
        else
        {
            legalBlackMoves(opponentPieces, occupied);
        }
    }

    private void legalMovesWithPseudoMoveRemoval()
    {
        long myKing = pieceList.piecesBB(turn, PieceType.KING);
        long occupied = occupied();
        long ownPieces = turn == Side.WHITE ? whitePieces() : blackPieces();
        long opponentPieces = occupied & ~ownPieces;
        long kingAttackers = opponentPieces & attackersTo(myKing, occupied);
        int attackersCount = PopulationCount.popCount(kingAttackers);
        long opponentTargets = turn == Side.WHITE ? blackTargets(occupied) : whiteTargets(occupied);
        long pinners = getPinners(occupied, ownPieces, myKing);

        generatePseudoMoves();

        int writeIndex = 0, readIndex = 0;
        int newMoveCount = 0;

        while (readIndex < moveCount)
        {
            if (legal(potentialMoves[readIndex], opponentTargets, pinners, kingAttackers, attackersCount))
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

    private long getPinners(long occupied, long ownPieces, long myKing)
    {
        long bqPinners = bishopQueenPinners(myKing, ownPieces, occupied, pieceList.piecesBB(turn.flip(), PieceType.QUEEN) | pieceList.piecesBB(turn.flip(), PieceType.BISHOP));
        long rqPinners = rookQueenPinners(myKing, ownPieces, occupied, pieceList.piecesBB(turn.flip(), PieceType.QUEEN) | pieceList.piecesBB(turn.flip(), PieceType.ROOK));
        return bqPinners | rqPinners;
    }

    public boolean isLegal(short move)
    {
        long occupied = occupied();
        long ownPieces = turn == Side.WHITE ? whitePieces() : blackPieces();
        long opponentPieces = occupied & ~ownPieces;
        long myKing = pieceList.piecesBB(turn, PieceType.KING);
        long kingAttackers = opponentPieces & attackersTo(myKing, occupied);
        int attackersCount = PopulationCount.popCount(kingAttackers);
        long opponentTargets = turn == Side.WHITE ? blackTargets(occupied) : whiteTargets(occupied);
        long pinners = getPinners(occupied, ownPieces, myKing);
        return legal(move, opponentTargets, pinners, kingAttackers, attackersCount);
    }

    private boolean legal(short move, long opponentTargets, long pinners, long attackersToKing, int attackersCount)
    {
        long king = pieceList.piecesBB(turn, PieceType.KING);
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
        return !isMovePinned(kingSq, pinners, from, to) && canPreventKingAttack(kingSq, attackersToKing, to, attackersCount);
    }

    private boolean canPreventKingAttack(int kingSq, long sliderAttackers, long moveTo, int attackersCount)
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
        long attackerBB = leftShift(1L, slider);
        long inBetweenSquares = InBetweenCache.getInstance().inBetweenSet(slider, kingSq);
        return intersects(moveTo, inBetweenSquares | attackerBB);
    }

    private boolean isMovePinned(int kingSq, long pinners, long moveFrom, long moveTo)
    {
        while (pinners != 0)
        {
            int pinner = BitScan.ls1b(pinners);
            long pinnerBB = leftShift(1L, pinner);
            long inBetweenSquares = InBetweenCache.getInstance().inBetweenSet(pinner, kingSq);
            if (intersects(inBetweenSquares, moveFrom))
            {
                return !intersects(moveTo, inBetweenSquares | pinnerBB);
            }
            pinners &= ~pinnerBB;
        }
        return false;
    }

    private void generatePseudoMoves()
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
        long squareBB = BoardUtil.squareBB(square);
        long targets = rookTargets(squareBB, occupancy);
        blockers &= targets;
        return targets ^ rookTargets(squareBB, occupancy ^ blockers);
    }

    private long xrayBishopAttacks(int square, long blockers, long occupancy)
    {
        long squareBB = BoardUtil.squareBB(square);
        long targets = bishopTargets(squareBB, occupancy);
        blockers &= targets;
        return targets ^ bishopTargets(squareBB, occupancy ^ blockers);
    }

    private long attackersTo(long squareBB, long occupied)
    {
        return (singleKingTargets(squareBB) & (BK() | WK()))
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
            knights &= ~leftShift(1L, ls1b);
            targets |= Knight.targets(ls1b);
        }

        return targets;
    }

    private long singleKingTargets(long king)
    {
        return King.targets(BitScan.ls1b(king));
    }

    private long kingTargets(long kings)
    {
        long targets = 0;
        while (kings != 0)
        {
            int ls1b = BitScan.ls1b(kings);
            kings &= ~leftShift(1L, ls1b);
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
            rooks &= (rooks - 1);
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
            bishops &= (bishops - 1);
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

    private void legalWhiteMoves(long opponentPieces, long occupied)
    {
        long ownPieces = whitePieces();
        long myKing = pieceList.piecesBB(Side.WHITE, PieceType.KING);
        int kingSq = BitScan.ls1b(myKing);
        long kingAttackers = opponentPieces & attackersTo(myKing, occupied);
        int attackersCount = PopulationCount.popCount(kingAttackers);
        long opponentTargets = blackTargets(occupied);
        long pinners = getPinners(occupied, ownPieces, myKing);

        if (attackersCount == 2)
        {
            legalKingMoves(WK(), opponentPieces, occupied, opponentTargets);
        }
        else
        {
            legalWhitePawnMoves(WP(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalQueenMoves(WQ(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalRookMoves(WR(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalBishopMoves(WB(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalKnightMoves(WN(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalWhiteCastles(occupied, opponentTargets);
            legalKingMoves(WK(), opponentPieces, occupied, opponentTargets);
        }
    }

    private void legalBlackMoves(long opponentPieces, long occupied)
    {
        long ownPieces = blackPieces();
        long myKing = pieceList.piecesBB(Side.BLACK, PieceType.KING);
        int kingSq = BitScan.ls1b(myKing);
        long kingAttackers = opponentPieces & attackersTo(myKing, occupied);
        int attackersCount = PopulationCount.popCount(kingAttackers);
        long opponentTargets = whiteTargets(occupied);
        long pinners = getPinners(occupied, ownPieces, myKing);

        if (attackersCount == 2)
        {
            legalKingMoves(BK(), opponentPieces, occupied, opponentTargets);
        }
        else
        {
            legalBlackPawnMoves(BP(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalQueenMoves(BQ(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalRookMoves(BR(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalBishopMoves(BB(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalKnightMoves(BN(), opponentPieces, occupied, kingSq, kingAttackers, attackersCount, pinners);
            legalBlackCastles(occupied, opponentTargets);
            legalKingMoves(BK(), opponentPieces, occupied, opponentTargets);

        }
    }

    private void legalKingMoves(long kingBB, long opponentPieces, long occupied, long opponentAttacks)
    {
        while (kingBB != 0)
        {
            int fromSq = BitScan.ls1b(kingBB);
            kingBB &= ~leftShift(1L, fromSq);
            long attackSet = King.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                if (!intersects(BoardUtil.squareBB(toSquare), opponentAttacks))
                {
                    short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                    addMove(Move.move(fromSq, toSquare, moveType));
                }
            }
        }
    }

    private void pseudoKingMoves(long kingBB, long opponentPieces, long occupied)
    {
        while (kingBB != 0)
        {
            int fromSq = BitScan.ls1b(kingBB);
            kingBB &= ~leftShift(1L, fromSq);
            long attackSet = King.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(fromSq, toSquare, moveType));
            }
        }
    }

    private void legalKnightMoves(long knightsBB, long opponentPieces, long occupied, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
    {
        while (knightsBB != 0)
        {
            int fromSq = BitScan.ls1b(knightsBB);
            knightsBB &= (knightsBB - 1);
            long attackSet = Knight.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                if (isMovePinned(kingSq, pinners, BoardUtil.squareBB(fromSq), BoardUtil.squareBB(toSquare))
                    || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(fromSq, toSquare, moveType));
            }
        }
    }

    private void pseudoKnightMoves(long knightsBB, long opponentPieces, long occupied)
    {
        while (knightsBB != 0)
        {
            int fromSq = BitScan.ls1b(knightsBB);
            knightsBB &= (knightsBB - 1);
            long attackSet = Knight.targets(fromSq);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(fromSq, toSquare, moveType));
            }
        }
    }

    private void legalRookMoves(long rooks, long opponentPieces, long occupied, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
    {
        while (rooks != 0)
        {
            int rook = BitScan.ls1b(rooks);
            rooks &= ~leftShift(1L, rook);
            long attackSet = MagicCache.getInstance().rookAttacks(rook, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                if (isMovePinned(kingSq, pinners, BoardUtil.squareBB(rook), BoardUtil.squareBB(toSquare))
                    || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(rook, toSquare, moveType));
            }
        }
    }

    private void pseudoRookMoves(long rooks, long opponentPieces, long occupied)
    {
        while (rooks != 0)
        {
            int rook = BitScan.ls1b(rooks);
            rooks &= ~leftShift(1L, rook);
            long attackSet = MagicCache.getInstance().rookAttacks(rook, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(rook, toSquare, moveType));
            }
        }
    }

    private void legalBishopMoves(long bishops, long opponentPieces, long occupied, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
    {
        while (bishops != 0)
        {
            int bishop = BitScan.ls1b(bishops);
            bishops &= (bishops - 1);
            long attackSet = MagicCache.getInstance().bishopAttacks(bishop, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                if (isMovePinned(kingSq, pinners, BoardUtil.squareBB(bishop), BoardUtil.squareBB(toSquare))
                    || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(bishop, toSquare, moveType));
            }
        }
    }

    private void pseudoBishopMoves(long bishops, long opponentPieces, long occupied)
    {
        while (bishops != 0)
        {
            int bishop = BitScan.ls1b(bishops);
            bishops &= (bishops - 1);
            long attackSet = MagicCache.getInstance().bishopAttacks(bishop, occupied);
            attackSet &= ~occupied | opponentPieces;
            while (attackSet != 0)
            {
                int toSquare = BitScan.ls1b(attackSet);
                attackSet &= ~BoardUtil.squareBB(toSquare);
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(bishop, toSquare, moveType));
            }
        }
    }

    private void legalQueenMoves(long queens, long opponentPieces, long occupied, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
    {
        while (queens != 0)
        {
            int queen = BitScan.ls1b(queens);
            queens &= (queens - 1);

            // rook attacks
            long rookAttackSets = MagicCache.getInstance().rookAttacks(queen, occupied);
            rookAttackSets &= ~occupied | opponentPieces;
            while (rookAttackSets != 0)
            {
                int toSquare = BitScan.ls1b(rookAttackSets);
                rookAttackSets &= ~BoardUtil.squareBB(toSquare);
                if (isMovePinned(kingSq, pinners, BoardUtil.squareBB(queen), BoardUtil.squareBB(toSquare))
                    || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(queen, toSquare, moveType));
            }

            // bishop attacks
            long bishopAttackSets = MagicCache.getInstance().bishopAttacks(queen, occupied);
            bishopAttackSets &= ~occupied | opponentPieces;
            while (bishopAttackSets != 0)
            {
                int toSquare = BitScan.ls1b(bishopAttackSets);
                bishopAttackSets &= ~BoardUtil.squareBB(toSquare);
                if (isMovePinned(kingSq, pinners, BoardUtil.squareBB(queen), BoardUtil.squareBB(toSquare))
                    || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
                {
                    continue;
                }

                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(queen, toSquare, moveType));
            }
        }
    }

    private void pseudoQueenMoves(long queens, long opponentPieces, long occupied)
    {
        while (queens != 0)
        {
            int queen = BitScan.ls1b(queens);
            queens &= (queens - 1);

            // rook attacks
            long rookAttackSets = MagicCache.getInstance().rookAttacks(queen, occupied);
            rookAttackSets &= ~occupied | opponentPieces;
            while (rookAttackSets != 0)
            {
                int toSquare = BitScan.ls1b(rookAttackSets);
                rookAttackSets &= ~BoardUtil.squareBB(toSquare);
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(queen, toSquare, moveType));
            }

            // bishop attacks
            long bishopAttackSets = MagicCache.getInstance().bishopAttacks(queen, occupied);
            bishopAttackSets &= ~occupied | opponentPieces;
            while (bishopAttackSets != 0)
            {
                int toSquare = BitScan.ls1b(bishopAttackSets);
                bishopAttackSets &= ~BoardUtil.squareBB(toSquare);
                short moveType = intersects(BoardUtil.squareBB(toSquare), opponentPieces) ? Move.CAPTURES : Move.QUIET_MOVE;
                addMove(Move.move(queen, toSquare, moveType));
            }
        }
    }

    private void legalWhiteCastles(long occupied, long opponentAttacks)
    {
        if (intersects(WK(), opponentAttacks))
        {
            return;
        }

        if (((W_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (W_KING_CASTLE_ATTACK_MASK & opponentAttacks) == 0
            && (WK() & BB_E1) != 0
            && (WR() & BB_H1) != 0
            && canKingSideCastleWhite())
        {
            addMove(Move.move(SQ_E1, SQ_G1, Move.KING_CASTLE));
        }

        if ((W_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (W_QUEEN_CASTLE_ATTACK_MASK & opponentAttacks) == 0
            && (WK() & BB_E1) != 0
            && (WR() & BB_A1) != 0
            && canQueenSideCastleWhite())
        {
            addMove(Move.move(SQ_E1, SQ_C1, Move.QUEEN_CASTLE));
        }
    }

    public boolean canQueenSideCastleWhite()
    {
        return canCastle(WQ_CASTLE_MASK);
    }

    public boolean canKingSideCastleWhite()
    {
        return canCastle(WK_CASTLE_MASK);
    }

    private void legalBlackCastles(long occupied, long opponentAttacks)
    {
        if (intersects(BK(), opponentAttacks))
        {
            return;
        }

        if (((B_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (B_KING_CASTLE_ATTACK_MASK & opponentAttacks) == 0
            && (BK() & BB_E8) != 0
            && (BR() & BB_H8) != 0
            && canKingSideCastleBlack())
        {
            addMove(Move.move(SQ_E8, SQ_G8, Move.KING_CASTLE));
        }

        if ((B_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (B_QUEEN_CASTLE_ATTACK_MASK & opponentAttacks) == 0
            && (BK() & BB_E8) != 0
            && (BR() & BB_A8) != 0
            && canQueenSideCastleBlack())
        {
            addMove(Move.move(SQ_E8, SQ_C8, Move.QUEEN_CASTLE));
        }
    }

    public boolean canQueenSideCastleBlack()
    {
        return canCastle(BQ_CASTLE_MASK);
    }

    private void pseudoWhiteCastles(long occupied)
    {
        if (((W_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (WK() & BB_E1) != 0
            && (WR() & BB_H1) != 0
            && canKingSideCastleWhite())
        {
            addMove(Move.move(SQ_E1, SQ_G1, Move.KING_CASTLE));
        }

        if ((W_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (WK() & BB_E1) != 0
            && (WR() & BB_A1) != 0
            && canQueenSideCastleWhite())
        {
            addMove(Move.move(SQ_E1, SQ_C1, Move.QUEEN_CASTLE));
        }
    }

    private void pseudoBlackCastles(long occupied)
    {
        if (((B_KING_CASTLE_CLEAR_MASK) & occupied) == 0
            && (BK() & BB_E8) != 0
            && (BR() & BB_H8) != 0
            && canKingSideCastleBlack())
        {
            addMove(Move.move(SQ_E8, SQ_G8, Move.KING_CASTLE));
        }

        if ((B_QUEEN_CASTLE_CLEAR_MASK & occupied) == 0
            && (BK() & BB_E8) != 0
            && (BR() & BB_A8) != 0
            && canQueenSideCastleBlack())
        {
            addMove(Move.move(SQ_E8, SQ_C8, Move.QUEEN_CASTLE));
        }
    }

    public boolean canKingSideCastleBlack()
    {
        return canCastle(BK_CASTLE_MASK);
    }

    private void legalWhitePawnMoves(long whitePawns, long opponentPieces, long occupied, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
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

        addLegalPawnMoves(singlePushes, Direction.NORTH, Move.QUIET_MOVE, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnMoves(doublePushes, Direction.NORTH * 2, Move.DOUBLE_PAWN_PUSH, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnMoves(attacksNE, Direction.NORTH_EAST, Move.CAPTURES, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnMoves(attacksNW, Direction.NORTH_WEST, Move.CAPTURES, kingSq, kingAttackers, kingAttackersCount, pinners);

        addLegalPawnPromotions(promotionsPush, Direction.NORTH, false, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnPromotions(promotionAttacksNE, Direction.NORTH_EAST, true, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnPromotions(promotionAttacksNW, Direction.NORTH_WEST, true, kingSq, kingAttackers, kingAttackersCount, pinners);

        addLegalEnPassantForWhite(occupied, kingSq, kingAttackers, kingAttackersCount, pinners);
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

    private void legalBlackPawnMoves(long blackPawns, long opponentPieces, long occupied, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
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

        addLegalPawnMoves(singlePushes, Direction.SOUTH, Move.QUIET_MOVE, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnMoves(doublePushes, Direction.SOUTH * 2, Move.DOUBLE_PAWN_PUSH, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnMoves(attacksSE, Direction.SOUTH_EAST, Move.CAPTURES, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnMoves(attacksSW, Direction.SOUTH_WEST, Move.CAPTURES, kingSq, kingAttackers, kingAttackersCount, pinners);

        addLegalPawnPromotions(promotionsPush, Direction.SOUTH, false, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnPromotions(promotionAttacksSE, Direction.SOUTH_EAST, true, kingSq, kingAttackers, kingAttackersCount, pinners);
        addLegalPawnPromotions(promotionAttacksSW, Direction.SOUTH_WEST, true, kingSq, kingAttackers, kingAttackersCount, pinners);

        addEnPassantForBlack(occupied, kingSq, kingAttackers, kingAttackersCount, pinners);
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
            targetSquares &= ~BoardUtil.squareBB(toSquare);

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

    private void addLegalPawnPromotions(long targetSquares, int shift, boolean isCapture, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
    {
        while (targetSquares != 0)
        {
            int toSquare = BitScan.ls1b(targetSquares);
            int fromSquare = toSquare - shift;
            targetSquares &= ~BoardUtil.squareBB(toSquare);

            if (isMovePinned(kingSq, pinners, BoardUtil.squareBB(fromSquare), BoardUtil.squareBB(toSquare))
                || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
            {
                continue;
            }

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

    private void addLegalEnPassantForWhite(long occupied, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
    {
        int epFileIndex = getEnPassantFile(irreversibleState) - 1;
        if (epFileIndex < 0)
        {
            return;
        }

        long targetPawnBB = BoardUtil.squareBB(epFileIndex, RANK_5);
        long targetSquareBB = BoardUtil.squareBB(epFileIndex, RANK_6);
        int targetSquare = BoardUtil.square(epFileIndex, RANK_6);

        if (!intersects(targetPawnBB, BP()) || intersects(targetSquareBB, occupied))
        {
            return;
        }

        if (intersects(eastOne(WP()), targetPawnBB))
        {
            if (!(isMovePinned(kingSq, pinners, BoardUtil.squareBB(targetSquare + Direction.SOUTH_WEST), BoardUtil.squareBB(targetSquare))
                  || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(targetSquare), kingAttackersCount))
                || kingAttackers == targetPawnBB) // single pawn attacker, which can be removed
            {
                addMove(Move.move(targetSquare + Direction.SOUTH_WEST, targetSquare, Move.EP_CAPTURE));
            }
        }

        if (intersects(westOne(WP()), targetPawnBB))
        {
            if (!(isMovePinned(kingSq, pinners, BoardUtil.squareBB(targetSquare + Direction.SOUTH_EAST), BoardUtil.squareBB(targetSquare))
                  || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(targetSquare), kingAttackersCount))
                || kingAttackers == targetPawnBB) // single pawn attacker, which can be removed
            {
                addMove(Move.move(targetSquare + Direction.SOUTH_EAST, targetSquare, Move.EP_CAPTURE));

            }
        }
    }

    private void addEnPassantForWhite(long occupied)
    {
        int epFileIndex = getEnPassantFile(irreversibleState) - 1;
        if (epFileIndex < 0)
        {
            return;
        }

        long targetPawnBB = BoardUtil.squareBB(epFileIndex, RANK_5);
        long targetSquareBB = BoardUtil.squareBB(epFileIndex, RANK_6);
        int targetSquare = BoardUtil.square(epFileIndex, RANK_6);

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

    private void addEnPassantForBlack(long occupied, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
    {
        int epFileIndex = getEnPassantFile(irreversibleState) - 1;
        if (epFileIndex < 0)
        {
            return;
        }

        long targetPawnBB = BoardUtil.squareBB(epFileIndex, RANK_4);
        long targetSquareBB = BoardUtil.squareBB(epFileIndex, RANK_3);
        int targetSquare = BoardUtil.square(epFileIndex, RANK_3);

        if (!intersects(targetPawnBB, WP()) || intersects(targetSquareBB, occupied))
        {
            return;
        }

        if (intersects(eastOne(BP()), targetPawnBB))
        {
            if (!(isMovePinned(kingSq, pinners, BoardUtil.squareBB(targetSquare + Direction.NORTH_WEST), BoardUtil.squareBB(targetSquare))
                  || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(targetSquare), kingAttackersCount))
                || kingAttackers == targetPawnBB) // single pawn attacker, which can be removed
            {
                addMove(Move.move(targetSquare + Direction.NORTH_WEST, targetSquare, Move.EP_CAPTURE));
            }
        }

        if (intersects(westOne(BP()), targetPawnBB))
        {
            if (!(isMovePinned(kingSq, pinners, BoardUtil.squareBB(targetSquare + Direction.NORTH_EAST), BoardUtil.squareBB(targetSquare))
                  || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(targetSquare), kingAttackersCount))
                || kingAttackers == targetPawnBB) // single pawn attacker, which can be removed
            {
                addMove(Move.move(targetSquare + Direction.NORTH_EAST, targetSquare, Move.EP_CAPTURE));
            }
        }
    }

    private void addEnPassantForBlack(long occupied)
    {
        int epFileIndex = getEnPassantFile(irreversibleState) - 1;
        if (epFileIndex < 0)
        {
            return;
        }

        long targetPawnBB = BoardUtil.squareBB(epFileIndex, RANK_4);
        long targetSquareBB = BoardUtil.squareBB(epFileIndex, RANK_3);
        int targetSquare = BoardUtil.square(epFileIndex, RANK_3);

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

    private void addLegalPawnMoves(long targetSquares, int shift, short moveType, int kingSq, long kingAttackers, int kingAttackersCount, long pinners)
    {
        while (targetSquares != 0)
        {
            int toSquare = BitScan.ls1b(targetSquares);
            int fromSquare = toSquare - shift;
            targetSquares &= ~BoardUtil.squareBB(toSquare);

            if (isMovePinned(kingSq, pinners, BoardUtil.squareBB(fromSquare), BoardUtil.squareBB(toSquare))
                || !canPreventKingAttack(kingSq, kingAttackers, BoardUtil.squareBB(toSquare), kingAttackersCount))
            {
                continue;
            }

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

    private void addPawnMoves(long targetSquares, int shift, short moveType)
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

    private long leftShift(long x, int s)
    {
        return x << s;
    }

    private long rightShift(long x, int s)
    {
        return x >>> s;
    }

    private long northOne(long bb)
    {
        return leftShift(bb, BOARD_DIM);
    }

    private long southOne(long bb)
    {
        return rightShift(bb, BOARD_DIM);
    }

    private long eastOne(long bb)
    {
        return leftShift(bb, 1) & ~BB_FILE_A;
    }

    private long westOne(long bb)
    {
        return rightShift(bb, 1) & ~BB_FILE_H;
    }

    private long NE1(long bb)
    {
        return leftShift(bb, 9) & ~BB_FILE_A;
    }

    private long NW1(long bb)
    {
        return leftShift(bb, 7) & ~BB_FILE_H;
    }

    private long SE1(long bb)
    {
        return rightShift(bb, 7) & ~BB_FILE_A;
    }

    private long SW1(long bb)
    {
        return rightShift(bb, 9) & ~BB_FILE_H;
    }

    private boolean intersects(long a, long b)
    {
        return (a & b) != 0;
    }

    private boolean canCastle(short castleMask)
    {
        return (getCastleRights(irreversibleState) & castleMask) != 0;
    }

    private short setCastleBit(short castleRights, short castleMask)
    {
        castleRights |= castleMask;
        return castleRights;
    }

    private short unsetCastleBit(short castleRights, short castleMask)
    {
        return castleRights &= ~castleMask;
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
        potentialMoves[moveCount++] = move;
    }

    public void debug()
    {
        System.out.println("state: " + irreversibleState);
        System.out.println("castle rights: " + getCastleRights(irreversibleState));
    }

    public int identify()
    {
        return pieceList.hashCode();
    }

    public PieceList getPieceList()
    {
        return pieceList;
    }
}
