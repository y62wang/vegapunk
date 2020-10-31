package com.y62wang.chess.engine.game;

import com.y62wang.chess.engine.Bitboard;
import com.y62wang.chess.engine.Move;
import com.y62wang.chess.engine.TranspositionTable;
import com.y62wang.chess.engine.search.AlphaBeta;
import com.y62wang.chess.engine.search.SearchDriver;
import com.y62wang.chess.engine.search.SearchResult;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Engine
{
    private final SearchDriver searchDriver;

    private GameState gameState;
    private EngineState engineState;
    private boolean debug;

    // functionality to add:
    // 0. configure option
    // 1. time management
    // -> i. don't time out, ii. strategy based on opponent time.
    // 2. current game state
    // -> current best move, nodes searched, depths
    // 4. current threads && search jobs
    // 5. pondering
    // 6. enable debugging
    // 7. provide information to engine

    public Engine()
    {
        engineState = new EngineState();
        engineState.tt = new TranspositionTable();
        searchDriver = new SearchDriver(new AlphaBeta(engineState.tt));
        newGame();
    }

    public void newGame()
    {
        GameState newGameState = new GameState();
        newGameState.setBoard(new Bitboard());
        newGameState.setComputationTime(Duration.ZERO);
        this.gameState = newGameState;
    }

    public String stopSearch()
    {
        SearchResult result = searchDriver.stop();
        if (result == null)
        {
            return null;
        }
        return Move.moveString(result.getMove());
    }

    public String search(SearchParameters searchParameters)
    {
        Duration timeLimit = searchParameters.getTimeLimit();
        SearchResult result = searchDriver.search(gameState.getBoard(), timeLimit.toMillis(), TimeUnit.MILLISECONDS);
        return Move.moveString(result.getMove());
    }

    public void setBoard(Bitboard board)
    {
        this.gameState.setBoard(board);
    }

    public void ponder()
    {
    }

    // UCI client uses this to configure the engine
    public void configure()
    {
    }

    public void playMove(String move)
    {
        gameState.getBoard().makeMove(move);
    }

    public void setDebugging(final boolean on)
    {
        this.debug = on;
    }

    public void fen(final String fenString)
    {
        this.setBoard(new Bitboard(fenString));
    }

    public GameState getGameState()
    {
        return gameState;
    }

}
