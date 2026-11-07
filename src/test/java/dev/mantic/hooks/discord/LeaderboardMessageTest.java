package dev.mantic.hooks.discord;

import com.google.gson.JsonObject;
import dev.mantic.hooks.leaderboard.Ranking;
import dev.mantic.hooks.leaderboard.TopRankings;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeaderboardMessageTest {

    private static final UUID RICH = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String LINE = "**{rank}.** {player} — {value}";

    @Test
    void ranksHighestFirstAndEscapesNames() {
        TopRankings ranked = new TopRankings(2);
        for (Ranking ranking : Arrays.asList(new Ranking("low", UUID.randomUUID(), 1), new Ranking("_rich_", RICH, 500),
                new Ranking("mid", UUID.randomUUID(), 50))) {
            if (ranked.accepts(ranking.getValue())) ranked.add(ranking);
        }
        List<Ranking> top = ranked.sorted();
        EmbedStyle style = new EmbedStyle("", "", "Top", 0xF1C40F, "", "", "", "", false);
        JsonObject payload = LeaderboardMessage.build(style, top, value -> "$" + (long) value, LINE, "None");
        JsonObject embed = payload.getAsJsonArray("embeds").get(0).getAsJsonObject();

        assertEquals("**1.** \\_rich\\_ — $500\n**2.** mid — $50", embed.get("description").getAsString());
        assertEquals(0xF1C40F, embed.get("color").getAsInt());
        assertEquals(0, payload.getAsJsonObject("allowed_mentions").getAsJsonArray("parse").size());
        for (String key : new String[]{"footer", "thumbnail", "image", "timestamp"}) assertFalse(embed.has(key), key);
        assertFalse(payload.has("username"));
    }

    @Test
    void fillsStyleFieldsWithTopPlayer() {
        EmbedStyle style = new EmbedStyle("Bot", "https://x/a.png", "Best: {top_player}", 1, "#1 {top_player}", "https://x/{top_uuid}/i",
                "https://mc-heads.net/avatar/{top_uuid}", "https://x/big.png", true);
        JsonObject payload = LeaderboardMessage.build(style, Collections.singletonList(new Ranking("Steve", RICH, 5)), String::valueOf, LINE, "None");
        JsonObject embed = payload.getAsJsonArray("embeds").get(0).getAsJsonObject();

        assertEquals("Bot", payload.get("username").getAsString());
        assertEquals("https://x/a.png", payload.get("avatar_url").getAsString());
        assertEquals("Best: Steve", embed.get("title").getAsString());
        assertEquals("#1 Steve", embed.getAsJsonObject("footer").get("text").getAsString());
        assertEquals("https://x/" + RICH + "/i", embed.getAsJsonObject("footer").get("icon_url").getAsString());
        assertEquals("https://mc-heads.net/avatar/" + RICH, embed.getAsJsonObject("thumbnail").get("url").getAsString());
        assertEquals("https://x/big.png", embed.getAsJsonObject("image").get("url").getAsString());
        assertFalse(embed.get("timestamp").getAsString().isEmpty());
    }

    @Test
    void emptyLeaderboardStillHasADescription() {
        EmbedStyle style = new EmbedStyle("", "", "Top", 0, "", "", "", "", false);
        JsonObject payload = LeaderboardMessage.build(style, Collections.emptyList(), String::valueOf, "{rank}", "No players to rank yet.");
        assertEquals("No players to rank yet.", payload.getAsJsonArray("embeds").get(0).getAsJsonObject().get("description").getAsString());
    }

    @Test
    void acceptsOnlyHttpsDiscordWebhooks() {
        DiscordWebhook.parse("https://discord.com/api/webhooks/1/abc");
        DiscordWebhook.parse("https://canary.discord.com/api/webhooks/1/abc");
        for (String bad : new String[]{"http://discord.com/api/webhooks/1/abc", "https://evil.com/api/webhooks/1/abc",
                "https://discord.com.evil.com/api/webhooks/1/abc", "https://user@discord.com/api/webhooks/1/abc",
                "https://discord.com:8443/api/webhooks/1/abc", "https://discord.com/channels/1", "not a url"}) {
            assertThrows(IllegalArgumentException.class, () -> DiscordWebhook.parse(bad), bad);
        }
    }
}
