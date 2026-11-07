package dev.mantic.hooks.leaderboard;

import java.util.UUID;

public final class Ranking {

    private final String name;
    private final UUID uuid;
    private final double value;

    public Ranking(String name, UUID uuid, double value) {
        this.name = name;
        this.uuid = uuid;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getValue() {
        return value;
    }
}
