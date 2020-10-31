package com.y62wang.chess.game.uci.commands;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterCommand
{
    private String name;
    private String code;
}
