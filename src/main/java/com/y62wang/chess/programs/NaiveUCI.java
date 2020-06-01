package com.y62wang.chess.programs;

import com.y62wang.chess.Bitboard;
import com.y62wang.chess.Move;
import com.y62wang.chess.search.AlphaBeta;
import com.y62wang.chess.search.AlphaBeta.Node;
import lombok.extern.log4j.Log4j2;

import java.util.Scanner;

@Log4j2
public class NaiveUCI
{
    static Bitboard mainBoard = new Bitboard();

    public static void main(String[] args)
    {
        log.info("UCI Client Started");
        try
        {
            mainLoop();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private static void mainLoop()
    {
        Scanner scanner = new Scanner(System.in);
        boolean condition = true;
        while (condition)
        {
            String line = scanner.nextLine();
            String[] tokens = line.split(" ");
            log.info(line + "\n");
            log.info(mainBoard.toString());
            if (line.equals("uci"))
            {
                System.out.println("id name vegapunk");
                System.out.println("id author Yang Wang");
                System.out.println("uciok");
            }
            else if (line.equals("isready"))
            {
                System.out.println("readyok");
            }
            else if (line.startsWith("setoption"))
            {
                System.out.println("uciok");
            }
            else if (line.startsWith("debug"))
            {

            }
            else if (line.startsWith("register"))
            {

            }
            else if (line.startsWith("ucinewgame"))
            {
                mainBoard = new Bitboard();
            }
            else if (line.startsWith("position"))
            {
                if (tokens.length == 1)
                {
                    continue;
                }
                if (tokens[1].equals("startpos"))
                {
                    mainBoard = new Bitboard();
                    if (tokens.length < 3)
                    {
                        continue;
                    }
                    if (tokens[2].equals("moves"))
                    {
                        // log(String.format("M(%s)", 1) + mainBoard.toString());
                        for (int i = 3; i < tokens.length; i++)
                        {
                            //final List<Move> moves = mainBoard.make();
                            //mainBoard = makeHumanMove(mainBoard, tokens[i], moves);
                            // log(String.format("M(%s)", (i-2)) + mainBoard.toString());
                        }
                        log.info(mainBoard.toString());
                    }
                    else if (tokens[2].equals("fen"))
                    {
                        mainBoard = new Bitboard(line.replace("position fen ", ""));
                    }
                }
                else if (tokens[1].equals("fen")) {
                    mainBoard = new Bitboard(line.replace("position fen ", ""));
                }
            }
            else if (line.startsWith("go"))
            {
                Bitboard board = mainBoard;
                Node node = AlphaBeta.search(board, 4 - (board.isWhiteTurn() ? 0 : 1));
                if (node == null)
                {
                    System.out.println(board);
                    System.out.println("GAME OVER");
                    throw new RuntimeException();
                }
                short move = node.move;
                System.out.println("bestmove " + Move.moveString(move));
                log.info("Engine Played: " + Move.moveString(move));
                board.makeMove(move);
            }
            else if (line.startsWith("stop"))
            {
            }
            else if (line.startsWith("ponderhit"))
            {

            }
            else if (line.startsWith("quit"))
            {
                System.exit(0);
            }
            else
            {
                log.error("I have no idea...");
            }
        }
    }

    public static class SearchRunnable implements Runnable
    {

        private final Bitboard bitboard;

        public SearchRunnable(Bitboard bitboard)
        {
            this.bitboard = bitboard;
        }

        @Override
        public void run()
        {
            makeComputerMove(bitboard);
        }

        public static void makeComputerMove(final Bitboard board)
        {
            Node node = AlphaBeta.search(board, 4 - (board.isWhiteTurn() ? 0 : 1));
            if (node == null)
            {
                System.out.println(board);
                System.out.println("GAME OVER");
                throw new RuntimeException();
            }
            short move = node.move;
            board.makeMove(move);
        }
    }

}
