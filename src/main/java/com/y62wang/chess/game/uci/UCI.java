package com.y62wang.chess.game.uci;

import com.y62wang.chess.game.uci.commands.GoCommand;
import com.y62wang.chess.game.uci.commands.PositionCommand;
import com.y62wang.chess.game.uci.commands.RegisterCommand;
import com.y62wang.chess.game.uci.commands.SetOptionCommand;

public interface UCI
{
    void UCI();

    void isReady();

    void debug(boolean on);

    void setOption(SetOptionCommand cmd);

    void registerLater();

    void register(RegisterCommand cmd);

    void ucinewgame();

    void position(PositionCommand cmd);

    void go(GoCommand cmd);

    void stop();

    void ponderHit();

    void quit();
}
