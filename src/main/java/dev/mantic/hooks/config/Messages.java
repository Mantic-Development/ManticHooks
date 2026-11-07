package dev.mantic.hooks.config;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class Messages {

    private String noPermission = "&cYou do not have permission to use this command.";
    private String usage = "&cUsage: /<command> <reload|post> [leaderboard]";
    private String reloaded = "&aReloaded ManticHooks, {count} leaderboard(s) active.";
    private String posting = "&aPosting leaderboard {name}.";
    private String alreadyPosting = "&eLeaderboard {name} is already being posted.";
    private String unknownLeaderboard = "&cNo active leaderboard named {name}. Active: {active}";
    private String discordLine = "**{rank}.** {player} — {value}";
    private String discordEmpty = "No players to rank yet.";

    public String getNoPermission() {
        return noPermission;
    }

    public String getUsage() {
        return usage;
    }

    public String getReloaded() {
        return reloaded;
    }

    public String getPosting() {
        return posting;
    }

    public String getAlreadyPosting() {
        return alreadyPosting;
    }

    public String getUnknownLeaderboard() {
        return unknownLeaderboard;
    }

    public String getDiscordLine() {
        return discordLine;
    }

    public String getDiscordEmpty() {
        return discordEmpty;
    }
}
