package dev.mantic.hooks;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches offline player names by UUID. For players the server has not cached, {@link OfflinePlayer#getName()}
 * reads and parses their player file, so repeated leaderboard posts would otherwise re-read the same files.
 * Safe to use from any thread.
 */
final class PlayerNames implements Listener {

    private final Map<UUID, String> names = new ConcurrentHashMap<>();

    /** The player's name, or null if the server does not know it. */
    String get(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        String name = names.get(uuid);
        if (name == null) {
            name = player.getName();
            if (name != null) {
                names.put(uuid, name);
            }
        }
        return name;
    }

    // A rename only becomes visible when the player joins, so refresh then.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        names.put(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
