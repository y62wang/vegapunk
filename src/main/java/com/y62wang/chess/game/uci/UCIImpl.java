package com.y62wang.chess.game.uci;

import com.y62wang.chess.engine.game.Engine;
import com.y62wang.chess.engine.game.SearchParameters;
import com.y62wang.chess.engine.timing.NaiveTimingStrategy;
import com.y62wang.chess.engine.timing.TimingStrategy;
import com.y62wang.chess.game.uci.commands.GoCommand;
import com.y62wang.chess.game.uci.commands.PositionCommand;
import com.y62wang.chess.game.uci.commands.RegisterCommand;
import com.y62wang.chess.game.uci.commands.SetOptionCommand;
import lombok.extern.log4j.Log4j2;

import java.io.PrintStream;
import java.time.Duration;

@Log4j2
public class UCIImpl implements UCI
{
    private Engine engine;
    private EngineInfo engineInfo;
    private PrintStream out;
    private TimingStrategy timingStrategy;

    public UCIImpl(Engine engine, EngineInfo engineInfo, PrintStream outputStream)
    {
        this.engine = engine;
        this.engineInfo = engineInfo;
        out = outputStream;
        timingStrategy = new NaiveTimingStrategy();
    }

    @Override
    public void UCI()
    {
        out.println("id name vegapunk");
        out.println("id author Yang Wang");
        out.println("uciok");
    }

    @Override
    public void isReady()
    {
        out.println("readyok");
    }

    @Override
    public void debug(boolean on)
    {
        engine.setDebugging(on);
    }

    @Override
    public void setOption(SetOptionCommand cmd)
    {
        log.warn("Unable to set option '{}' with value '{}'", cmd.getName(), cmd.getValue());
    }

    @Override
    public void registerLater()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register(RegisterCommand cmd)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void ucinewgame()
    {
        engine.newGame();
    }

    @Override
    public void position(PositionCommand cmd)
    {
        if ("startpos".equals(cmd.getPosition()))
        {
            engine.newGame();
            for (String move : cmd.getMoves())
            {
                engine.playMove(move);
            }
        }
        else
        {
            engine.setBoard(cmd.getPosition());
        }

        log.info("board: " + engine.getGameState().getBoard());
    }

    @Override
    public void go(GoCommand cmd)
    {
        String bestMove = null;

        if (cmd.isPonder())
        {
            throw new IllegalArgumentException("Engine does not support ponder yet");
        }
        else if (cmd.getSearchMoves() != null)
        {
            throw new IllegalArgumentException("Cannot do searchmoves yet");
        }
        else if (cmd.getMate() != null)
        {
            throw new IllegalArgumentException("Cannot search for mate yet");
        }
        else if (cmd.getNodes() != null)
        {
            throw new IllegalArgumentException("Cannot search for nodes yet");
        }
        else if (cmd.isInfinite())
        {
            bestMove = engine.search(SearchParameters.builder().timeLimit(Duration.ofDays(Long.MAX_VALUE)).build());
        }
        else
        {
            Duration duration = timingStrategy.decideSearchTime(engine.getGameState(), cmd);
            bestMove = engine.search(SearchParameters.builder().timeLimit(duration).build());
        }

        if (bestMove != null)
        {
            out.println("bestmove " + bestMove);
        }
    }

    @Override
    public void stop()
    {
        String move = engine.stopSearch();
        out.println("bestmove " + move);
    }

    @Override
    public void ponderHit()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void quit()
    {
        System.exit(0);
    }
}
