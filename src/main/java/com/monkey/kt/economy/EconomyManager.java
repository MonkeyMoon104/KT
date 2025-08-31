package com.monkey.kt.economy;

import com.monkey.kt.KT;
import com.monkey.kt.economy.storage.KillCoinsStorage;
import com.monkey.kt.storage.DatabaseManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class EconomyManager {

    private final KT plugin;
    private final DatabaseManager databaseManager;
    private KillCoinsEco internalEconomy;
    private Economy vaultEconomy;
    private boolean useInternal;
    private boolean initialized = false;

    public EconomyManager(KT plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.useInternal = plugin.getConfig().getBoolean("economy.use_internal", true);
    }
    public void initialize() {
        if (initialized) {
            plugin.getLogger().warning("EconomyManager already initialized!");
            return;
        }

        if (useInternal) {
            setupInternalEconomy();
        } else {
            setupVaultEconomy();
            if (vaultEconomy == null) {
                plugin.getLogger().warning("Failed to setup Vault economy, falling back to internal economy");
                useInternal = true;
                setupInternalEconomy();
            }
        }

        initialized = true;
        plugin.getLogger().info("Economy system initialized using: " +
                (useInternal ? "Internal KillCoins" : "Vault (" + vaultEconomy.getName() + ")"));
    }

    private void setupInternalEconomy() {
        try {
            if (databaseManager == null || !databaseManager.isConnected()) {
                throw new IllegalStateException("Database manager not available");
            }

            KillCoinsStorage storage = new KillCoinsStorage(
                    databaseManager.getExecutor(),
                    databaseManager.getDialect(),
                    databaseManager.getDataSource()
            );

            this.internalEconomy = new KillCoinsEco(plugin, storage);
            plugin.getLogger().info("Internal economy (KillCoins) initialized with " +
                    databaseManager.getDialect().name() + " database");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize internal economy", e);
            throw new RuntimeException("Internal economy initialization failed", e);
        }
    }

    private void setupVaultEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Cannot use external economy.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No Vault economy provider found!");
            return;
        }

        vaultEconomy = rsp.getProvider();

        setupInternalEconomy();
    }

    public boolean isEnabled() {
        if (!initialized) {
            plugin.getLogger().warning("Economy system not initialized!");
            return false;
        }

        if (useInternal) {
            return internalEconomy.isEnabled();
        } else {
            return vaultEconomy != null && plugin.getConfig().getBoolean("economy.enabled", true);
        }
    }

    public String currencySymbol() {
        if (useInternal) {
            return internalEconomy.currencySymbol();
        } else {
            return vaultEconomy != null ? vaultEconomy.currencyNameSingular() : "Money";
        }
    }

    public double getBalance(OfflinePlayer player) {
        if (useInternal) {
            return internalEconomy.getBalance(player);
        } else {
            return vaultEconomy != null ? vaultEconomy.getBalance(player) : 0.0;
        }
    }

    public void setBalance(OfflinePlayer player, double amount) {
        if (useInternal) {
            internalEconomy.setBalance(player, amount);
        } else if (vaultEconomy != null) {
            double currentBalance = vaultEconomy.getBalance(player);
            if (currentBalance > amount) {
                vaultEconomy.withdrawPlayer(player, currentBalance - amount);
            } else if (currentBalance < amount) {
                vaultEconomy.depositPlayer(player, amount - currentBalance);
            }
        }
    }

    public void add(OfflinePlayer player, double amount) {
        if (amount == 0) return;

        if (useInternal) {
            internalEconomy.add(player, amount);
        } else if (vaultEconomy != null) {
            vaultEconomy.depositPlayer(player, amount);
        }
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (useInternal) {
            return internalEconomy.withdraw(player, amount);
        } else if (vaultEconomy != null) {
            return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (useInternal) {
            return internalEconomy.has(player, amount);
        } else if (vaultEconomy != null) {
            return vaultEconomy.has(player, amount);
        }
        return false;
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
        if (useInternal) {
            return internalEconomy.hasBoughtEffect(player, effectKey);
        } else {
            return internalEconomy.getStorage().hasBought(player.getUniqueId(), effectKey);
        }
    }

    public boolean tryBuyEffect(Player player, String effectKey) {
        double price = getEffectPrice(effectKey);
        if (price <= 0) {
            if (useInternal) {
                internalEconomy.getStorage().markBought(player.getUniqueId(), effectKey);
            } else {
                internalEconomy.getStorage().markBought(player.getUniqueId(), effectKey);
            }
            return true;
        }

        if (!has(player, price)) return false;

        if (withdraw(player, price)) {
            if (useInternal) {
                internalEconomy.getStorage().markBought(player.getUniqueId(), effectKey);
            } else {
                internalEconomy.getStorage().markBought(player.getUniqueId(), effectKey);
            }
            return true;
        }
        return false;
    }

    public int getTotalAccounts() {
        return internalEconomy != null ? internalEconomy.getTotalAccounts() : -1;
    }

    public double getTotalEconomy() {
        return internalEconomy != null ? internalEconomy.getTotalEconomy() : -1;
    }

    public int getTotalPurchases() {
        return internalEconomy != null ? internalEconomy.getTotalPurchases() : -1;
    }

    public java.util.List<java.util.Map.Entry<java.util.UUID, Double>> getTopBalances(int limit) {
        if (internalEconomy == null) return new java.util.ArrayList<>();
        return internalEconomy.getTopBalances(limit);
    }

    public CompletableFuture<Void> resetAllBalances() {
        if (internalEconomy == null) {
            return CompletableFuture.completedFuture(null);
        }
        return internalEconomy.resetAllBalances();
    }

    public CompletableFuture<Void> resetAllPurchases() {
        if (internalEconomy == null) {
            return CompletableFuture.completedFuture(null);
        }
        return internalEconomy.resetAllPurchases();
    }

    public void forceSyncAll() {
        if (internalEconomy != null) {
            internalEconomy.forceSyncAll();
        }
    }

    public boolean testConnection() {
        return databaseManager != null && databaseManager.testConnection();
    }

    public String getDatabaseInfo() {
        if (databaseManager == null) return "No database";
        return String.format("Database: %s, Connected: %s",
                databaseManager.getDialect().name(),
                databaseManager.isConnected());
    }

    public boolean isUsingInternal() {
        return useInternal;
    }

    public KillCoinsEco getInternalEconomy() {
        return internalEconomy;
    }

    public Economy getVaultEconomy() {
        return vaultEconomy;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void shutdown() {
        if (internalEconomy != null) {
            plugin.getLogger().info("Syncing economy data before shutdown...");
            internalEconomy.forceSyncAll();
        }
        initialized = false;
    }
}