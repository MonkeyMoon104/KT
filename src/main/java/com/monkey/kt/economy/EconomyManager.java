package com.monkey.kt.economy;

import com.monkey.kt.KT;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final KT plugin;
    private final KillCoinsEco internalEconomy;
    private Economy vaultEconomy;
    private boolean useInternal;

    public EconomyManager(KT plugin, KillCoinsEco internalEconomy) {
        this.plugin = plugin;
        this.internalEconomy = internalEconomy;
        this.useInternal = plugin.getConfig().getBoolean("economy.use_internal", true);

        if (!useInternal) {
            setupVaultEconomy();
        }
    }

    private void setupVaultEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Falling back to internal economy.");
            useInternal = true;
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No Vault economy provider found! Falling back to internal economy.");
            useInternal = true;
            return;
        }

        vaultEconomy = rsp.getProvider();
        plugin.getLogger().info("Using external economy via Vault: " + vaultEconomy.getName());
    }

    public boolean isEnabled() {
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

    public boolean isUsingInternal() {
        return useInternal;
    }

    public KillCoinsEco getInternalEconomy() {
        return internalEconomy;
    }

    public Economy getVaultEconomy() {
        return vaultEconomy;
    }
}