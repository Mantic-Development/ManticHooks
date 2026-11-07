package dev.mantic.hooks.config;

import de.exlll.configlib.BukkitYamlConfiguration;
import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.ElementType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class HooksConfig extends BukkitYamlConfiguration {

    private static final Pattern COMMAND_NAME = Pattern.compile("[a-z0-9_-]+");

    public HooksConfig(Plugin plugin) {
        super(plugin, "config");
    }

    @Comment({
            "Admin command. Changes apply on /<command> reload; players may need to rejoin for tab completion to update.",
            "command-name and command-aliases may only use a-z, 0-9, _ and -."
    })
    private String commandName = "mantichooks";
    // configlib infers simple element types; @ElementType is only for @ConfigurationElement classes.
    private List<String> commandAliases = new ArrayList<>(Collections.singletonList("hooks"));
    @Comment("Permission needed for every subcommand. Defaults to op unless your permissions plugin grants it.")
    private String commandPermission = "mantichooks.admin";

    @Comment({"",
            "Chat messages support & colour codes and <command> (the label used).",
            "Placeholders: reloaded {count}; posting, already-posting {name}; unknown-leaderboard {name} {active}.",
            "discord-line is one leaderboard row in Discord markdown: {rank} {player} {value}."
    })
    private Messages messages = new Messages();

    @Comment({"",
            "Leaderboards posted to Discord webhooks on a timer.",
            "",
            "name             = id used by /<command> post <name>",
            "webhook-url      = Discord channel settings > Integrations > Webhooks > Copy Webhook URL (keep it secret)",
            "source           = what players are ranked by:",
            "                   vault-balance = Vault economy balance",
            "                   placeholder   = any numeric PlaceholderAPI placeholder, set in 'placeholder'",
            "placeholder      = used when source is placeholder, e.g. %statistic_player_kills% or %statistic_deaths%.",
            "                   The expansion must answer for offline players, or only players it knows are ranked.",
            "limit            = how many players to list (1-25)",
            "interval-minutes = minutes between posts",
            "read-async       = read balances off the main thread. Only enable if your economy plugin is thread-safe;",
            "                   when false, reads are spread over several ticks so the server never stalls.",
            "",
            "Embed look (empty = left out). title, footer and the URLs may use {top_player} and {top_uuid}:",
            "webhook-name     = sender name shown in Discord (empty = the webhook's own name)",
            "webhook-avatar   = sender avatar image URL",
            "title            = embed title",
            "colour           = embed colour as hex, e.g. #F1C40F",
            "footer           = footer text",
            "footer-icon      = small image URL next to the footer",
            "thumbnail        = image URL top-right, e.g. https://mc-heads.net/avatar/{top_uuid} for the #1 player's head",
            "image            = large image URL under the leaderboard",
            "timestamp        = show the post time",
            "",
            "A leaderboard with an empty webhook-url is skipped."
    })
    @ElementType(LeaderboardFeedConfig.class)
    private List<LeaderboardFeedConfig> leaderboards = new ArrayList<>(Collections.singletonList(new LeaderboardFeedConfig()));

    /** @throws IllegalArgumentException naming the first invalid command setting */
    public void validateCommand() {
        if (commandName == null || !COMMAND_NAME.matcher(commandName).matches()) {
            throw new IllegalArgumentException("command-name '" + commandName + "' may only use a-z, 0-9, _ and -");
        }
        for (String alias : getCommandAliases()) {
            if (!COMMAND_NAME.matcher(alias).matches()) {
                throw new IllegalArgumentException("command-aliases entry '" + alias + "' may only use a-z, 0-9, _ and -");
            }
        }
        if (commandPermission == null || commandPermission.trim().isEmpty()) {
            throw new IllegalArgumentException("command-permission must not be empty");
        }
    }

    public String getCommandName() {
        return commandName;
    }

    public List<String> getCommandAliases() {
        return commandAliases == null ? Collections.<String>emptyList() : commandAliases;
    }

    public String getCommandPermission() {
        return commandPermission.trim();
    }

    public Messages getMessages() {
        return messages == null ? new Messages() : messages;
    }

    public List<LeaderboardFeedConfig> getLeaderboards() {
        return leaderboards == null ? Collections.<LeaderboardFeedConfig>emptyList() : leaderboards;
    }
}
