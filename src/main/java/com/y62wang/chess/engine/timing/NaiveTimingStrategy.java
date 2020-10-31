package com.y62wang.chess.engine.timing;

import com.y62wang.chess.engine.Bitboard;
import com.y62wang.chess.engine.enums.Side;
import com.y62wang.chess.engine.game.GameState;
import com.y62wang.chess.game.uci.commands.GoCommand;

import java.time.Duration;

public class NaiveTimingStrategy implements TimingStrategy
{
    @Override
    public Duration decideSearchTime(final GameState state, final GoCommand command)
    {
        Bitboard board = state.getBoard();
        if (command.getMovetime() != null)
        {
            return Duration.ofMillis(command.getMovetime());
        }
        else if (board.getTurn() == Side.WHITE && command.getWtime() != null)
        {
            return Duration.ofMillis(command.getWtime());
        }
        else if (board.getTurn() == Side.BLACK && command.getBtime() != null)
        {
            return Duration.ofMillis(command.getBtime());
        }
        return Duration.ofDays(Long.MAX_VALUE);
    }
}
