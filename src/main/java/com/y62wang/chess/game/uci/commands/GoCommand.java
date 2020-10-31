package com.y62wang.chess.game.uci.commands;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GoCommand
{
    private boolean ponder;
    private boolean infinite;
    private Long mate;
    private Long movetime;
    private Long nodes;
    private Long movesToGo;
    private Long winc;
    private Long binc;
    private Long wtime;
    private Long btime;
    private Long depth;
    private List<String> searchMoves;

}
