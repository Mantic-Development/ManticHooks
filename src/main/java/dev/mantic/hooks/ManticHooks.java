package dev.mantic.hooks;

import dev.mantic.hooks.config.HooksConfig;
import dev.mantic.hooks.config.LeaderboardFeedConfig;
import dev.mantic.hooks.config.Messages;
import dev.mantic.hooks.leaderboard.PlaceholderSource;
import dev.mantic.hooks.leaderboard.StatSource;
import dev.mantic.hooks.leaderboard.VaultBalanceSource;
import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.ManticPlugin;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class ManticHooks extends ManticPlugin {

    public enum PostResult { STARTED, BUSY, UNKNOWN }

    private final Map<String, Function<LeaderboardFeedConfig, StatSource>> sources = new LinkedHashMap<>();
    private final Map<String, Feed> feeds = new LinkedHashMap<>();
    private final PlayerNames playerNames = new PlayerNames();
    private HooksConfig config;
    private HooksCommand command;

    @Override
    public void onEnable() {
        VaultBalanceSource vault = new VaultBalanceSource(ManticLib.get().getVault());
        sources.put("vault-balance", settings -> vault);
        sources.put("placeholder", settings -> PlaceholderSource.create(settings.getPlaceholder()));
        getServer().getPluginManager().registerEvents(playerNames, this);
        config = new HooksConfig(this);
        reload();
    }

    /** Re-reads config.yml, re-registers the command and restarts every leaderboard timer. Returns how many leaderboards are active. */
    public int reload() {
        getServer().getScheduler().cancelTasks(this);
        feeds.clear();
        config.loadAndSave();
        registerCommand();
        for (LeaderboardFeedConfig entry : config.getLeaderboards()) {
            if (!entry.isConfigured()) {
                getLogger().info("Leaderboard '" + entry.getName() + "' has no webhook-url, skipping.");
                continue;
            }
            try {
                Feed feed = entry.toFeed(sources);
                if (feeds.putIfAbsent(feed.getName(), feed) != null) {
                    getLogger().warning("Leaderboard name '" + feed.getName() + "' is used twice, skipping the second.");
                    continue;
                }
                getServer().getScheduler().runTaskTimer(this, () -> LeaderboardJob.start(this, feed, config.getMessages()),
                        feed.getPeriodTicks(), feed.getPeriodTicks());
            } catch (IllegalArgumentException e) {
                getLogger().warning("Leaderboard '" + entry.getName() + "' is disabled: " + e.getMessage());
            }
        }
        return feeds.size();
    }

    private void registerCommand() {
        try {
            config.validateCommand();
        } catch (IllegalArgumentException e) {
            // Keep the working command, if any; leaderboards still post either way.
            getLogger().warning("Command settings ignored: " + e.getMessage());
            return;
        }
        String permission = config.getCommandPermission();
        PluginManager plugins = getServer().getPluginManager();
        if (plugins.getPermission(permission) == null) {
            plugins.addPermission(new Permission(permission, "ManticHooks admin command", PermissionDefault.OP));
        }
        if (command != null) {
            command.unregister();
        }
        command = new HooksCommand(this, config.getCommandName(), config.getCommandAliases(), permission, config.getMessages());
        command.register();
    }

    PlayerNames getPlayerNames() {
        return playerNames;
    }

    public Messages getMessages() {
        return config.getMessages();
    }

    public Collection<String> getFeedNames() {
        return Collections.unmodifiableSet(feeds.keySet());
    }

    /** Posts the named leaderboard now. Main thread only. */
    public PostResult post(String name) {
        Feed feed = feeds.get(name);
        if (feed == null) {
            return PostResult.UNKNOWN;
        }
        return LeaderboardJob.start(this, feed, config.getMessages()) ? PostResult.STARTED : PostResult.BUSY;
    }
}
