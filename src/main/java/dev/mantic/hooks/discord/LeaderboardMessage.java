package dev.mantic.hooks.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.mantic.hooks.Placeholders;
import dev.mantic.hooks.leaderboard.Ranking;

import java.time.Instant;
import java.util.List;
import java.util.function.DoubleFunction;

/** Builds the webhook payload for a leaderboard. Uses only Gson API that 1.8 servers already ship. */
public final class LeaderboardMessage {

    private static final String MARKDOWN = "\\*_~`|>[]";
    // Discord's documented limits; longer values make it reject the whole message.
    private static final int MAX_USERNAME = 80;
    private static final int MAX_TITLE = 256;
    private static final int MAX_DESCRIPTION = 4096;
    private static final int MAX_FOOTER = 2048;

    private LeaderboardMessage() {
    }

    /** {@code line} is a Discord-markdown row template with {rank}, {player} and {value}; player and value are escaped. */
    public static JsonObject build(EmbedStyle style, List<Ranking> rankings, DoubleFunction<String> format, String line, String empty) {
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < rankings.size(); i++) {
            Ranking ranking = rankings.get(i);
            if (i > 0) {
                description.append('\n');
            }
            description.append(Placeholders.apply(line, "rank", i + 1, "player", escape(ranking.getName()),
                    "value", escape(format.apply(ranking.getValue()))));
        }

        Ranking top = rankings.isEmpty() ? null : rankings.get(0);
        Object[] tops = {"top_player", top == null ? "" : top.getName(), "top_uuid", top == null ? "" : top.getUuid()};

        JsonObject embed = new JsonObject();
        putText(embed, "title", Placeholders.apply(style.getTitle(), tops), MAX_TITLE);
        embed.addProperty("color", style.getColour());
        embed.addProperty("description", truncate(rankings.isEmpty() ? empty : description.toString(), MAX_DESCRIPTION));
        String footer = Placeholders.apply(style.getFooter(), tops);
        String footerIcon = Placeholders.apply(style.getFooterIcon(), tops);
        if (!footer.isEmpty()) {
            JsonObject footerJson = new JsonObject();
            footerJson.addProperty("text", truncate(footer, MAX_FOOTER));
            putText(footerJson, "icon_url", footerIcon, Integer.MAX_VALUE);
            embed.add("footer", footerJson);
        }
        putUrl(embed, "thumbnail", Placeholders.apply(style.getThumbnail(), tops));
        putUrl(embed, "image", Placeholders.apply(style.getImage(), tops));
        if (style.isTimestamp()) {
            embed.addProperty("timestamp", Instant.now().toString());
        }

        JsonArray embeds = new JsonArray();
        embeds.add(embed);

        // Player names and placeholder output must never ping anyone.
        JsonObject allowedMentions = new JsonObject();
        allowedMentions.add("parse", new JsonArray());

        JsonObject payload = new JsonObject();
        putText(payload, "username", style.getWebhookName(), MAX_USERNAME);
        putText(payload, "avatar_url", style.getWebhookAvatar(), Integer.MAX_VALUE);
        payload.add("embeds", embeds);
        payload.add("allowed_mentions", allowedMentions);
        return payload;
    }

    static String escape(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (MARKDOWN.indexOf(c) >= 0) {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    private static void putText(JsonObject json, String key, String value, int max) {
        if (value != null && !value.isEmpty()) {
            json.addProperty(key, truncate(value, max));
        }
    }

    private static void putUrl(JsonObject json, String key, String url) {
        if (url != null && !url.isEmpty()) {
            JsonObject object = new JsonObject();
            object.addProperty("url", url);
            json.add(key, object);
        }
    }

    private static String truncate(String text, int max) {
        String value = text == null ? "" : text;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
