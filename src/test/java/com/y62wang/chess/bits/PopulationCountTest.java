package com.y62wang.chess.bits;

import com.google.common.base.Stopwatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class PopulationCountTest
{
    @Test
    public void testRandomPopCount()
    {
        Random random = new Random(0);
        random.longs(1000, Long.MIN_VALUE, Long.MAX_VALUE).forEach(number ->validatePopCount(number));
    }

    @Test
    public void testPerformance() {
        PopulationCount.popCount(1);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Random random = new Random(0);
        random.longs(100000000, 0, Long.MAX_VALUE).forEach(number ->PopulationCount.popCount(number));
        stopwatch.stop();
        System.out.println("Elapsed Time: " + stopwatch.elapsed());
    }

    private void validatePopCount(final long number)
    {
        String message = "Population Count for " + number + " is wrong.";
        final int expected = Long.bitCount(number);
        Assert.assertEquals(message, expected, PopulationCount.popCount(number));
    }
}
