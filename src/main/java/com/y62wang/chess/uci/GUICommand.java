package com.y62wang.chess.uci;

public enum GUICommand
{
    UCI("uci"),
    DEBUG("debug"),
    IS_READY("isready"),
    SET_OPTION("setoption"),
    REGISTER("register"),
    UCI_NEW_GAME("ucinewgame"),
    POSITION("position"),
    GO("go"),
    STOP("stop"),
    PONDER_HIT("ponderhit"),
    QUIT("quit");



    private final String command;

    GUICommand(final String command)
    {
        this.command = command;
    }
}
