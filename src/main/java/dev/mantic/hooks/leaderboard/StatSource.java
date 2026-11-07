package dev.mantic.hooks.leaderboard;

import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.OptionalDouble;

/** A statistic players can be ranked by. Register new ones in {@code ManticHooks#onEnable}. */
public interface StatSource {

    /** Always called on the main thread. The players to rank, or an empty list if the source is unavailable. */
    List<OfflinePlayer> players();

    /**
     * One player's value, or empty if they have none. Runs on the main thread, or on an async thread
     * when the leaderboard sets read-async, so implementations must not touch world or entity state.
     */
    OptionalDouble value(OfflinePlayer player);

    /** Runs on the same thread as {@link #value}. */
    String format(double value);
}
