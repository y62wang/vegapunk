package com.y62wang.chess.engine.game;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
@Builder
public class SearchParameters
{
    private Duration timeLimit;
    private List<String> moves;
}
