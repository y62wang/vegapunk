package com.y62wang.chess.game.programs;

import com.y62wang.chess.engine.game.Engine;
import com.y62wang.chess.game.uci.EngineInfo;
import com.y62wang.chess.game.uci.UCIImpl;
import com.y62wang.chess.game.uci.UCIRunner;
import lombok.extern.log4j.Log4j2;

import java.util.Scanner;

@Log4j2
public class UCIMain
{
    public static void main(String[] args)
    {
        try
        {
            log.info("UCI Engine Started");
            Scanner scanner = new Scanner(System.in);
            UCIRunner uciRunner = new UCIRunner(scanner, new UCIImpl(new Engine(), new EngineInfo(), System.out));
            uciRunner.run();
        }
        catch (Exception ex)
        {
            log.error("UCI Engine Crashed...", ex);
            throw ex;
        }
    }
}

