package dev.mantic.hooks.leaderboard;

import me.fullpage.manticlib.integrations.VaultIntegration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

public final class VaultBalanceSource implements StatSource {

    private final VaultIntegration vault;

    public VaultBalanceSource(VaultIntegration vault) {
        this.vault = vault;
    }

    @Override
    public List<OfflinePlayer> players() {
        if (economy() == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(Bukkit.getOfflinePlayers());
    }

    @Override
    public OptionalDouble value(OfflinePlayer player) {
        Economy economy = economy();
        return economy != null && economy.hasAccount(player) ? OptionalDouble.of(economy.getBalance(player)) : OptionalDouble.empty();
    }

    @Override
    public String format(double value) {
        return vault.format(value);
    }

    private Economy economy() {
        return vault.isActive() ? vault.getEconomy() : null;
    }
}
