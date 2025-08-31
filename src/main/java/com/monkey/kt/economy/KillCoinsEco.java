package com.monkey.kt.economy;

import com.monkey.kt.KT;
import com.monkey.kt.economy.storage.KillCoinsStorage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

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
        storage.setBalance(player.getUniqueId(), Math.max(0, amount));
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

    public double killReward(Entity entity) {
        if (entity instanceof Player) {
            return plugin.getConfig().getDouble("economy.reward.settings.player_kill", 0D);
        } else {
            return plugin.getConfig().getDouble("economy.reward.settings.mob_kill", 0D);
        }
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

    public int getTotalAccounts() {
        return storage.getAccountsCount();
    }

    public double getTotalEconomy() {
        return storage.getTotalBalance();
    }

    public int getTotalPurchases() {
        return storage.getPurchasesCount();
    }

    public CompletableFuture<Void> resetAllBalances() {
        return storage.clearAllBalances();
    }

    public CompletableFuture<Void> resetAllPurchases() {
        return storage.clearAllPurchases();
    }

    public void forceSyncAll() {
        storage.forceSyncAll();
    }

    public CompletableFuture<Void> batchSetBalances(java.util.Map<java.util.UUID, Double> balances) {
        return storage.batchSaveBalances(balances);
    }

    public CompletableFuture<Void> batchGrantPurchases(java.util.Map<java.util.UUID, java.util.Set<String>> purchases) {
        return storage.batchSavePurchases(purchases);
    }

    public java.util.List<java.util.Map.Entry<java.util.UUID, Double>> getTopBalances(int limit) {
        return storage.getAllBalances().entrySet().stream()
                .sorted(java.util.Map.Entry.<java.util.UUID, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    public KillCoinsStorage getStorage() {
        return storage;
    }
}
