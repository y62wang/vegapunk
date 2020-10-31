package com.y62wang.chess.engine.search;

import com.google.common.base.Verify;
import com.y62wang.chess.engine.Bitboard;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Log4j2
public class SearchDriver
{
    private SearchAlgorithm searchAlgorithm;
    private ExecutorService executorService;

    private SearchConfiguration configuration;

    private Future<SearchNode> future;
    private SearchResult intermediateResult;

    public SearchDriver(SearchAlgorithm searchAlgorithm)
    {
        executorService = Executors.newFixedThreadPool(1);
        this.searchAlgorithm = searchAlgorithm;
    }

    public void configure(SearchConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public SearchResult search(Bitboard board, long timeout, TimeUnit timeUnit)
    {
        Verify.verify(future == null || future.isDone() || future.isCancelled());
        intermediateResult = new SearchResult(( short ) -1, -1);
        try
        {
            if (false == true)
            {
                future = executorService.submit(() -> searchAlgorithm.iterativeDeepening(board, 8, intermediateResult));
                SearchNode searchNode = future.get(timeout, timeUnit);
                return SearchResult.builder()
                        .move(searchNode.move)
                        .score(searchNode.score)
                        .build();
            }
            else
            {
                SearchNode searchNode = searchAlgorithm.iterativeDeepening(board, 8, intermediateResult);
                return SearchResult.builder()
                        .move(searchNode.move)
                        .score(searchNode.score)
                        .build();
            }
        }
        catch (InterruptedException e)
        {
            log.error("search interrupted", e);
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
            log.error("execution failed", e);
            log.error("execution failed", e.getCause());
        }
        catch (TimeoutException e)
        {
            log.error("search timeout", e);
            e.printStackTrace();
        }

        return intermediateResult;
    }

    public SearchResult stop()
    {
        if (future != null && !future.isDone())
        {
            future.cancel(true);
        }
        return intermediateResult;
    }
}
