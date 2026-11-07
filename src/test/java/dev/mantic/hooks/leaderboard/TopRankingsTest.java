package dev.mantic.hooks.leaderboard;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TopRankingsTest {

    @Test
    void keepsTheHighestInOrder() {
        Random random = new Random(42);
        List<Double> values = new ArrayList<>();
        TopRankings top = new TopRankings(10);
        for (int i = 0; i < 20_000; i++) {
            double value = random.nextInt(1_000_000);
            values.add(value);
            if (top.accepts(value)) top.add(new Ranking("p" + i, UUID.randomUUID(), value));
        }
        values.sort(Collections.reverseOrder());

        List<Ranking> sorted = top.sorted();
        assertEquals(10, sorted.size());
        for (int i = 0; i < 10; i++) assertEquals(values.get(i), sorted.get(i).getValue());
    }

    @Test
    void rejectsNaNAndValuesBelowAFullList() {
        TopRankings top = new TopRankings(1);
        assertFalse(top.accepts(Double.NaN));
        top.add(new Ranking("a", UUID.randomUUID(), 5));
        assertFalse(top.accepts(5));
        assertFalse(top.accepts(4));
    }
}
