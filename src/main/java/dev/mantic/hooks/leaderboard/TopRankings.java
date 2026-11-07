package dev.mantic.hooks.leaderboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Keeps the {@code limit} highest rankings seen, in O(limit) memory. Not thread-safe. */
public final class TopRankings {

    private static final Comparator<Ranking> BY_VALUE = Comparator.comparingDouble(Ranking::getValue);

    private final int limit;
    private final PriorityQueue<Ranking> lowestFirst;

    public TopRankings(int limit) {
        this.limit = limit;
        this.lowestFirst = new PriorityQueue<>(limit + 1, BY_VALUE);
    }

    /** True if a ranking with this value would make the list; check before building one to skip the work. */
    public boolean accepts(double value) {
        return !Double.isNaN(value) && (lowestFirst.size() < limit || value > lowestFirst.peek().getValue());
    }

    public void add(Ranking ranking) {
        lowestFirst.add(ranking);
        if (lowestFirst.size() > limit) {
            lowestFirst.poll();
        }
    }

    /** Highest first. */
    public List<Ranking> sorted() {
        List<Ranking> sorted = new ArrayList<>(lowestFirst);
        sorted.sort(BY_VALUE.reversed());
        return sorted;
    }
}
