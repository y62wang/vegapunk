package com.y62wang.chess.game.uci;

import com.y62wang.chess.game.uci.commands.GoCommand;
import com.y62wang.chess.game.uci.commands.PositionCommand;
import com.y62wang.chess.game.uci.commands.SetOptionCommand;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Log4j2
public class UCIRunner
{
    private Scanner scanner;
    private UCI uci;

    public UCIRunner(Scanner scanner, UCI uci)
    {
        this.scanner = scanner;
        this.uci = uci;
    }

    public void run()
    {

        while (true)
        {
            String line = scanner.nextLine();
            String[] tokens = line.split(" ");
            String cmdString = tokens[0];
            log.info("input: " + line);
            if ("uci".equals(cmdString))
            {
                uci.UCI();
            }
            else if ("debug".equals(cmdString))
            {
                boolean enableDebug = tokens.length == 2 && "enableDebug".equals(tokens[1]);
                uci.debug(enableDebug);
            }
            else if ("isready".equals(cmdString))
            {
                uci.isReady();
            }
            else if ("setoption".equals(cmdString))
            {
                if (tokens.length != 5)
                {
                    throw new IllegalArgumentException("Unable to handle setoption cmd: " + line);
                }

                SetOptionCommand setOptionCmd = SetOptionCommand.builder()
                        .name(tokens[2])
                        .value(tokens[4])
                        .build();
                uci.setOption(setOptionCmd);
            }
            else if ("register".equals(cmdString))
            {
                throw new UnsupportedOperationException();
            }
            else if ("ucinewgame".equals(cmdString))
            {
                uci.ucinewgame();
            }
            else if ("position".equals(cmdString))
            {
                String positionType = tokens[1];
                if ("startpos".equals(positionType))
                {
                    List<String> moves = new ArrayList<>();
                    for (int i = 3; i < tokens.length; i++)
                    {
                        moves.add(tokens[i]);
                    }
                    PositionCommand positionCmd = PositionCommand.builder().position(positionType).moves(moves).build();
                    uci.position(positionCmd);
                }
                else if ("fen".equals(positionType))
                {
                    PositionCommand positionCmd = PositionCommand.builder().position(line.replace("position fen ", "")).moves(new ArrayList<>()).build();
                    uci.position(positionCmd);
                }
            }
            else if ("go".equals(cmdString))
            {
                uci.go(buildGoCommand(tokens));
            }
            else if ("stop".equals(cmdString))
            {
                uci.stop();
            }
            else if ("ponderhit".equals(cmdString))
            {
            }
            else if ("quit".equals(cmdString))
            {
                uci.quit();
            }
            else
            {
                log.error("Command not supported: '{}'", cmdString);
            }
        }
    }

    private GoCommand buildGoCommand(String[] cmdTokens)
    {
        GoCommand cmd = new GoCommand();
        if (indexOf("infinite", cmdTokens) > 0)
        {
            cmd.setInfinite(true);
        }
        if (indexOf("searchmoves", cmdTokens) > 0)
        {
            int index = indexOf("searchmoves", cmdTokens) + 1;
            List<String> moves = new ArrayList<>();
            for (int i = index; i < cmdTokens.length; i++) moves.add(cmdTokens[i]);
            cmd.setSearchMoves(moves);
        }
        if (indexOf("ponder", cmdTokens) > 0)
        {
            cmd.setPonder(true);
        }
        if (indexOf("wtime", cmdTokens) > 0)
        {
            int index = indexOf("wtime", cmdTokens) + 1;
            cmd.setWtime(Long.parseLong(cmdTokens[index]));
        }
        if (indexOf("btime", cmdTokens) > 0)
        {
            int index = indexOf("btime", cmdTokens) + 1;
            cmd.setBtime(Long.parseLong(cmdTokens[index]));
        }
        if (indexOf("winc", cmdTokens) > 0)
        {
            int index = indexOf("winc", cmdTokens) + 1;
            cmd.setWinc(Long.parseLong(cmdTokens[index]));
        }
        if (indexOf("binc", cmdTokens) > 0)
        {
            int index = indexOf("binc", cmdTokens) + 1;
            cmd.setBinc(Long.parseLong(cmdTokens[index]));
        }
        if (indexOf("movestogo", cmdTokens) > 0)
        {
            int index = indexOf("binc", cmdTokens) + 1;
            cmd.setMovesToGo(Long.parseLong(cmdTokens[index]));
        }
        if (indexOf("depth", cmdTokens) > 0)
        {
            int index = indexOf("depth", cmdTokens) + 1;
            cmd.setDepth(Long.parseLong(cmdTokens[index]));
        }
        if (indexOf("nodes", cmdTokens) > 0)
        {
            int index = indexOf("nodes", cmdTokens) + 1;
            cmd.setNodes(Long.parseLong(cmdTokens[index]));
        }
        if (indexOf("mate", cmdTokens) > 0)
        {
            int index = indexOf("mate", cmdTokens) + 1;
            cmd.setMate(Long.parseLong(cmdTokens[index]));
        }
        if (indexOf("movetime", cmdTokens) > 0)
        {
            int index = indexOf("movetime", cmdTokens) + 1;
            cmd.setMovetime(Long.parseLong(cmdTokens[index]));
        }
        return cmd;
    }

    private int indexOf(String token, String[] tokens)
    {
        int result = -1;

        for (int i = 0; i < tokens.length; i++)
        {
            if (token.equals(tokens[i]))
            {
                return i;
            }
        }
        return result;
    }
}
