package dev.mantic.hooks.leaderboard;

import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholderSourceTest {

    @Test
    void readsNumbersThroughFormatting() {
        assertEquals(OptionalDouble.of(1234.5), PlaceholderSource.parse("&a1,234.5"));
        assertEquals(OptionalDouble.of(42), PlaceholderSource.parse("§l 42 "));
        assertEquals(OptionalDouble.of(-3), PlaceholderSource.parse("-3"));
    }

    @Test
    void skipsNonNumbers() {
        for (String raw : new String[]{null, "", "%statistic_deaths%", "1.2k", "NaN", "Infinity", "none"}) {
            assertEquals(OptionalDouble.empty(), PlaceholderSource.parse(raw), String.valueOf(raw));
        }
    }
}
