package dev.mantic.hooks;

import dev.mantic.hooks.discord.DiscordWebhook;
import dev.mantic.hooks.discord.EmbedStyle;
import dev.mantic.hooks.leaderboard.StatSource;

import java.util.concurrent.atomic.AtomicBoolean;

/** A validated leaderboard ready to post. */
public final class Feed {

    private final String name;
    private final DiscordWebhook webhook;
    private final StatSource source;
    private final EmbedStyle style;
    private final int limit;
    private final long periodTicks;
    private final boolean readAsync;
    private final AtomicBoolean busy = new AtomicBoolean();

    public Feed(String name, DiscordWebhook webhook, StatSource source, EmbedStyle style, int limit, long periodTicks, boolean readAsync) {
        this.name = name;
        this.webhook = webhook;
        this.source = source;
        this.style = style;
        this.limit = limit;
        this.periodTicks = periodTicks;
        this.readAsync = readAsync;
    }

    public String getName() {
        return name;
    }

    public DiscordWebhook getWebhook() {
        return webhook;
    }

    public StatSource getSource() {
        return source;
    }

    public EmbedStyle getStyle() {
        return style;
    }

    public int getLimit() {
        return limit;
    }

    public long getPeriodTicks() {
        return periodTicks;
    }

    public boolean isReadAsync() {
        return readAsync;
    }

    /** True while a post of this feed is being read or sent. */
    AtomicBoolean getBusy() {
        return busy;
    }
}
