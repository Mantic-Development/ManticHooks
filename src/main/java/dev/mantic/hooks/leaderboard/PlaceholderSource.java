package dev.mantic.hooks.leaderboard;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.regex.Pattern;

/** Ranks players by any numeric PlaceholderAPI placeholder, e.g. %statistic_player_kills%. */
public final class PlaceholderSource implements StatSource {

    private static final Pattern PLACEHOLDER = Pattern.compile("%[^%\\s]+%");
    private static final Pattern COLOR_CODE = Pattern.compile("(?i)[§&][0-9A-FK-ORX]");

    private final String placeholder;
    private final String identifier;
    private final String params;
    // Looked up once per post on the main thread, then called directly for every player, skipping PlaceholderAPI's
    // per-call parsing. Null falls back to PlaceholderAPI.setPlaceholders.
    private volatile PlaceholderExpansion expansion;

    private PlaceholderSource(String placeholder) {
        this.placeholder = placeholder;
        String inner = placeholder.substring(1, placeholder.length() - 1);
        int split = inner.indexOf('_');
        this.identifier = (split < 0 ? inner : inner.substring(0, split)).toLowerCase(Locale.ROOT);
        this.params = split < 0 ? "" : inner.substring(split + 1);
    }

    /** @throws IllegalArgumentException if PlaceholderAPI is missing or the placeholder is malformed */
    public static PlaceholderSource create(String placeholder) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            throw new IllegalArgumentException("source 'placeholder' needs PlaceholderAPI installed");
        }
        if (!PLACEHOLDER.matcher(placeholder).matches()) {
            throw new IllegalArgumentException("placeholder must look like %expansion_name%, got '" + placeholder + "'");
        }
        return new PlaceholderSource(placeholder);
    }

    @Override
    public List<OfflinePlayer> players() {
        expansion = findExpansion();
        return Arrays.asList(Bukkit.getOfflinePlayers());
    }

    @Override
    public OptionalDouble value(OfflinePlayer player) {
        PlaceholderExpansion direct = expansion;
        return parse(direct != null ? direct.onRequest(player, params) : PlaceholderAPI.setPlaceholders(player, placeholder));
    }

    private PlaceholderExpansion findExpansion() {
        try {
            return PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(identifier);
        } catch (LinkageError e) {
            // PlaceholderAPI older than 2.10.10 has no expansion manager API.
            return null;
        }
    }

    @Override
    public String format(double value) {
        // DecimalFormat is not thread-safe, so build one per call; this runs at most 25 times per post.
        return new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(value);
    }

    /**
     * Reads a placeholder result as a number, ignoring colour codes, spaces and thousands separators.
     * Empty when the expansion returned text, an unparsed placeholder, or nothing (common for offline players).
     */
    // shortcut: treats ',' as a thousands separator, so "1,5" reads as 15; add a decimal-comma option if a locale needs it.
    static OptionalDouble parse(String raw) {
        if (raw == null) {
            return OptionalDouble.empty();
        }
        String cleaned = COLOR_CODE.matcher(raw).replaceAll("").replace(",", "").replace(" ", "").trim();
        if (cleaned.isEmpty()) {
            return OptionalDouble.empty();
        }
        try {
            double value = Double.parseDouble(cleaned);
            return Double.isNaN(value) || Double.isInfinite(value) ? OptionalDouble.empty() : OptionalDouble.of(value);
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }
}
