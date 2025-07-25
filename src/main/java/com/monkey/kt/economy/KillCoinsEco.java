package com.monkey.kt.economy;

import com.monkey.kt.KT;
import com.monkey.kt.economy.storage.KillCoinsStorage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class KillCoinsEco {

    private final KT plugin;
    private final KillCoinsStorage storage;

    public KillCoinsEco(KT plugin, KillCoinsStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("economy.enabled", true);
    }

    public String currencySymbol() {
        return plugin.getConfig().getString("economy.currency.symbol", "KC");
    }

    public double getBalance(OfflinePlayer player) {
        ensureAccount(player);
        return storage.getBalance(player.getUniqueId());
    }

    public void setBalance(OfflinePlayer player, double amount) {
        ensureAccount(player);
        storage.setBalance(player.getUniqueId(), amount);
    }

    public void add(OfflinePlayer player, double amount) {
        if (amount == 0) return;
        ensureAccount(player);
        storage.add(player.getUniqueId(), amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        ensureAccount(player);
        double bal = storage.getBalance(player.getUniqueId());
        if (bal < amount) return false;
        storage.add(player.getUniqueId(), -amount);
        return true;
    }

    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    public double killReward() {
        return plugin.getConfig().getDouble("economy.reward.kill", 0D);
    }

    public double getEffectPrice(String effectKey) {
        return plugin.getConfig().getDouble("effects." + effectKey + ".price", 0D);
    }

    public boolean hasBoughtEffect(Player player, String effectKey) {
        return storage.hasBought(player.getUniqueId(), effectKey);
    }

    public boolean tryBuyEffect(Player player, String effectKey) {
        double price = getEffectPrice(effectKey);
        if (price <= 0) {
            storage.markBought(player.getUniqueId(), effectKey);
            return true;
        }
        if (!has(player, price)) return false;
        if (withdraw(player, price)) {
            storage.markBought(player.getUniqueId(), effectKey);
            return true;
        }
        return false;
    }

    private void ensureAccount(OfflinePlayer player) {
        storage.createIfNotExists(player.getUniqueId(),
                plugin.getConfig().getDouble("economy.starting-balance", 0D));
    }
}
