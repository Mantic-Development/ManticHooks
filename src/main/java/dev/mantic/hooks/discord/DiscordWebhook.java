package dev.mantic.hooks.discord;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** A validated Discord webhook. The URL is a secret, so it is never logged or exposed. */
public final class DiscordWebhook {

    private static final Set<String> HOSTS = new HashSet<>(Arrays.asList("discord.com", "discordapp.com", "ptb.discord.com", "canary.discord.com"));
    private static final int TIMEOUT_MILLIS = 15_000;

    private final URL url;

    private DiscordWebhook(URL url) {
        this.url = url;
    }

    /** @throws IllegalArgumentException if {@code raw} is not an https Discord webhook URL */
    public static DiscordWebhook parse(String raw) {
        URI uri;
        try {
            uri = new URI(raw.trim());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("webhook-url is not a valid URL");
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        String path = uri.getPath() == null ? "" : uri.getPath();
        boolean valid = "https".equalsIgnoreCase(uri.getScheme()) && HOSTS.contains(host) && path.startsWith("/api/webhooks/")
                && uri.getUserInfo() == null && (uri.getPort() == -1 || uri.getPort() == 443);
        if (!valid) {
            throw new IllegalArgumentException("webhook-url must be an https://discord.com/api/webhooks/... URL");
        }
        try {
            return new DiscordWebhook(uri.toURL());
        } catch (IOException e) {
            throw new IllegalArgumentException("webhook-url is not a valid URL");
        }
    }

    /** Blocks on the network, so call it off the main thread. Returns the HTTP status. */
    public int send(JsonObject payload) throws IOException {
        byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("User-Agent", "ManticHooks (https://github.com/Mantic-Development/ManticHooks)");
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body);
            }
            return connection.getResponseCode();
        } finally {
            connection.disconnect();
        }
    }
}
