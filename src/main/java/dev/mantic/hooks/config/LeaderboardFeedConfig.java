package dev.mantic.hooks.config;

import de.exlll.configlib.annotation.ConfigurationElement;
import dev.mantic.hooks.Feed;
import dev.mantic.hooks.discord.DiscordWebhook;
import dev.mantic.hooks.discord.EmbedStyle;
import dev.mantic.hooks.leaderboard.StatSource;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

@ConfigurationElement
public final class LeaderboardFeedConfig {

    private static final Pattern HEX_COLOUR = Pattern.compile("#?[0-9a-fA-F]{6}");
    private static final Pattern WEB_URL = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    private static final int MAX_LIMIT = 25;

    private String name = "money";
    private String webhookUrl = "";
    private String source = "vault-balance";
    private String placeholder = "";
    private int limit = 10;
    private int intervalMinutes = 60;
    private boolean readAsync = false;

    private String webhookName = "";
    private String webhookAvatar = "";
    private String title = "Richest Players";
    private String colour = "#F1C40F";
    private String footer = "ManticHooks";
    private String footerIcon = "";
    private String thumbnail = "https://mc-heads.net/avatar/{top_uuid}";
    private String image = "";
    private boolean timestamp = true;

    public String getName() {
        return name;
    }

    public String getPlaceholder() {
        return placeholder == null ? "" : placeholder.trim();
    }

    public boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.trim().isEmpty();
    }

    /**
     * @param sources factories by source id; a factory throws IllegalArgumentException for settings it cannot use
     * @throws IllegalArgumentException naming the first invalid setting
     */
    public Feed toFeed(Map<String, Function<LeaderboardFeedConfig, StatSource>> sources) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        Function<LeaderboardFeedConfig, StatSource> factory = sources.get(source);
        if (factory == null) {
            throw new IllegalArgumentException("unknown source '" + source + "', expected one of " + sources.keySet());
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
        if (intervalMinutes < 1) {
            throw new IllegalArgumentException("interval-minutes must be at least 1");
        }
        return new Feed(name.trim(), DiscordWebhook.parse(webhookUrl), factory.apply(this), style(), limit,
                intervalMinutes * 60L * 20L, readAsync);
    }

    private EmbedStyle style() {
        if (colour == null || !HEX_COLOUR.matcher(colour).matches()) {
            throw new IllegalArgumentException("colour must be a hex colour like #F1C40F");
        }
        String username = text(webhookName);
        String lower = username.toLowerCase(Locale.ROOT);
        // Discord rejects every message whose username contains these words.
        if (lower.contains("discord") || lower.contains("clyde")) {
            throw new IllegalArgumentException("webhook-name must not contain 'discord' or 'clyde'");
        }
        int rgb = Integer.parseInt(colour.startsWith("#") ? colour.substring(1) : colour, 16);
        return new EmbedStyle(username, url("webhook-avatar", webhookAvatar), text(title), rgb, text(footer),
                url("footer-icon", footerIcon), url("thumbnail", thumbnail), url("image", image), timestamp);
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    /** Discord rejects the whole message when an image URL is not http(s), so catch it at load time. */
    private static String url(String key, String value) {
        String url = text(value).trim();
        if (!url.isEmpty() && !WEB_URL.matcher(url).matches()) {
            throw new IllegalArgumentException(key + " must be an http(s) URL or empty");
        }
        return url;
    }
}
