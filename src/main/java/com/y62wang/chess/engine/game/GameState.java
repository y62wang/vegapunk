package com.y62wang.chess.engine.game;

import com.y62wang.chess.engine.Bitboard;
import lombok.Data;

import java.time.Duration;

@Data
public class GameState
{
    private Bitboard board;
    private Duration computationTime;
}
