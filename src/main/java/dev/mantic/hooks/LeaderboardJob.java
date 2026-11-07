package dev.mantic.hooks;

import com.google.gson.JsonObject;
import dev.mantic.hooks.config.Messages;
import dev.mantic.hooks.discord.LeaderboardMessage;
import dev.mantic.hooks.leaderboard.Ranking;
import dev.mantic.hooks.leaderboard.StatSource;
import dev.mantic.hooks.leaderboard.TopRankings;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.logging.Level;

/** Reads one leaderboard and posts it. Holds the feed's busy flag from start until the post finishes or fails. */
final class LeaderboardJob {

    // Main-thread reads stop after this much of each 50ms tick and continue next tick.
    private static final long TICK_BUDGET_NANOS = 2_000_000L;

    private final ManticHooks plugin;
    private final Feed feed;
    private final Messages messages;
    private final StatSource source;
    private final PlayerNames names;
    private final Iterator<OfflinePlayer> players;
    private final TopRankings top;

    /** {@code players} must not change while the job runs; sources return a fresh list per post. */
    private LeaderboardJob(ManticHooks plugin, Feed feed, Messages messages, List<OfflinePlayer> players) {
        this.plugin = plugin;
        this.feed = feed;
        this.messages = messages;
        this.source = feed.getSource();
        this.names = plugin.getPlayerNames();
        this.players = players.iterator();
        this.top = new TopRankings(feed.getLimit());
    }

    /** Main thread only. Returns false if this feed is already being posted. */
    static boolean start(ManticHooks plugin, Feed feed, Messages messages) {
        if (!feed.getBusy().compareAndSet(false, true)) {
            return false;
        }
        try {
            LeaderboardJob job = new LeaderboardJob(plugin, feed, messages, feed.getSource().players());
            if (feed.isReadAsync()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, job::readAllAndSend);
            } else {
                job.readOverTicks();
            }
        } catch (RuntimeException e) {
            feed.getBusy().set(false);
            plugin.getLogger().log(Level.WARNING, "Could not read leaderboard '" + feed.getName() + "'", e);
        }
        return true;
    }

    private void readAllAndSend() {
        JsonObject payload;
        try {
            while (players.hasNext()) {
                read(players.next());
            }
            payload = buildPayload();
        } catch (RuntimeException e) {
            fail(e);
            return;
        }
        send(payload);
    }

    private void readOverTicks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                JsonObject payload;
                try {
                    long deadline = System.nanoTime() + TICK_BUDGET_NANOS;
                    while (players.hasNext() && System.nanoTime() < deadline) {
                        read(players.next());
                    }
                    if (players.hasNext()) {
                        return;
                    }
                    cancel();
                    payload = buildPayload();
                } catch (RuntimeException e) {
                    cancel();
                    fail(e);
                    return;
                }
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> send(payload));
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void read(OfflinePlayer player) {
        OptionalDouble value = source.value(player);
        // Most players don't make the list, so only they skip the name lookup and the Ranking allocation.
        if (!value.isPresent() || !top.accepts(value.getAsDouble())) {
            return;
        }
        String name = names.get(player);
        if (name != null) {
            top.add(new Ranking(name, player.getUniqueId(), value.getAsDouble()));
        }
    }

    private JsonObject buildPayload() {
        return LeaderboardMessage.build(feed.getStyle(), top.sorted(), source::format,
                messages.getDiscordLine(), messages.getDiscordEmpty());
    }

    private void send(JsonObject payload) {
        try {
            int status = feed.getWebhook().send(payload);
            if (status / 100 != 2) {
                plugin.getLogger().warning("Discord rejected leaderboard '" + feed.getName() + "' (HTTP " + status + ").");
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not post leaderboard '" + feed.getName() + "': " + e);
        } finally {
            feed.getBusy().set(false);
        }
    }

    private void fail(RuntimeException e) {
        feed.getBusy().set(false);
        plugin.getLogger().log(Level.WARNING, "Could not read leaderboard '" + feed.getName() + "'", e);
    }
}
