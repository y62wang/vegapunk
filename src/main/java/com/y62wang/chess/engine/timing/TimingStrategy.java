package com.y62wang.chess.engine.timing;

import com.y62wang.chess.engine.game.GameState;
import com.y62wang.chess.game.uci.commands.GoCommand;

import java.time.Duration;

public interface TimingStrategy
{
    Duration decideSearchTime(GameState gameState, GoCommand command);
}
