package com.y62wang.chess.game.uci.commands;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PositionCommand
{
    private String position;
    private List<String> moves;
}
