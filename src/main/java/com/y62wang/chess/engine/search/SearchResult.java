package com.y62wang.chess.engine.search;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResult
{
    private short move;
    private double score;
}
