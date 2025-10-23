package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.gui.GUIManager;
import com.monkey.kt.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements SubCommand {

    private final KT plugin;
    private final GUIManager guiManager;

    public ReloadCommand(KT plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "kt.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eReloading KT configuration..."));

        EconomyManager currentEconomyManager = plugin.getEconomyManager();
        boolean wasUsingInternal = currentEconomyManager != null ? currentEconomyManager.isUsingInternal() : true;
        String previousProvider = getEconomyProviderName(currentEconomyManager);

        plugin.reloadConfig();

        if (plugin.getCustomEffectLoader() != null) {
            plugin.getCustomEffectLoader().reloadCustomEffects();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&6Custom effects reloaded!"));
        }

        if (plugin.getEffectRegistry() != null) {
            plugin.getEffectRegistry().loadEffects(true);
        }

        cleanupObsoleteEffects();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&6Loaded " + com.monkey.kt.effects.KillEffectFactory.getRegisteredEffects().size() + " total effects"));

        boolean databaseChanged = checkDatabaseConfigChanged();

        boolean newUseInternal = plugin.getConfig().getBoolean("economy.use_internal", true);

        if (wasUsingInternal != newUseInternal || databaseChanged || currentEconomyManager == null || !currentEconomyManager.isInitialized()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&6Economy type changing from " + (wasUsingInternal ? "Internal" : "External") +
                            " to " + (newUseInternal ? "Internal" : "External") + "..."));

            try {
                if (currentEconomyManager != null) {
                    currentEconomyManager.shutdown();
                }

                if (databaseChanged) {
                    reloadDatabaseManager();
                }

                EconomyManager newEconomyManager = new EconomyManager(plugin, plugin.getDatabaseManager());

                newEconomyManager.initialize();

                if (!newEconomyManager.isInitialized()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&cFailed to initialize economy manager! Check console for errors."));
                    return;
                }

                plugin.setEconomyManager(newEconomyManager);

                updateEconomyReferences(newEconomyManager);

                String newProvider = getEconomyProviderName(newEconomyManager);

                if (newEconomyManager.isUsingInternal() && !newUseInternal) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&cWarning: Requested external economy but fell back to internal. " +
                                    "Check if Vault and economy plugin are properly installed."));
                    plugin.getLogger().warning("Failed to setup external economy, using internal fallback. " +
                            "Vault status: " + (plugin.getServer().getPluginManager().getPlugin("Vault") != null ? "Found" : "Not found"));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&aEconomy provider successfully changed from &e" + previousProvider + " &ato &e" + newProvider));
                }

                plugin.getLogger().info("Economy type changed from " + previousProvider + " to " + newProvider);

            } catch (Exception e) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&cError during economy system reload: " + e.getMessage()));
                plugin.getLogger().severe("Failed to reload economy system: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            String newProvider = getEconomyProviderName(plugin.getEconomyManager());
            if (!previousProvider.equals(newProvider)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&6Economy provider changed from &e" + previousProvider + " &ato &e" + newProvider));
            }
        }

        reloadResourcePackForPlayers();

        if (plugin.getStatusLogger() != null) {
            plugin.getStatusLogger().logReload();
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.config_reloaded", "&aConfiguration reloaded successfully!")));
    }

    private void cleanupObsoleteEffects() {
        try {
            java.util.Set<String> validEffects = new java.util.HashSet<>(
                    com.monkey.kt.effects.KillEffectFactory.getRegisteredEffects()
            );

            java.util.Map<java.util.UUID, String> allEffects =
                    com.monkey.kt.storage.EffectStorage.getAllEffects();

            int removed = 0;
            for (java.util.Map.Entry<java.util.UUID, String> entry : allEffects.entrySet()) {
                String effectName = entry.getValue();
                if (!validEffects.contains(effectName.toLowerCase())) {
                    com.monkey.kt.storage.EffectStorage.removeEffect(entry.getKey());
                    removed++;
                    plugin.getLogger().info("Removed obsolete effect '" + effectName +
                            "' from player " + entry.getKey());
                }
            }

            if (removed > 0) {
                plugin.getLogger().info("Cleaned up " + removed + " obsolete effects");
            }

            EconomyManager eco = plugin.getEconomyManager();
            if (eco != null && eco.getInternalEconomy() != null) {
                cleanupObsoletePurchases(validEffects, eco);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error cleaning up obsolete effects: " + e.getMessage());
        }
    }

    private void cleanupObsoletePurchases(java.util.Set<String> validEffects, EconomyManager eco) {
        try {
            com.monkey.kt.economy.storage.KillCoinsStorage storage =
                    eco.getInternalEconomy().getStorage();

            plugin.getDatabaseManager().getExecutor().executeTransaction(connection -> {
                String deleteSql = "DELETE FROM killcoins_purchases WHERE effect = ?";
                try (java.sql.PreparedStatement ps = connection.prepareStatement(deleteSql)) {

                    java.util.Map<java.util.UUID, java.util.Set<String>> allPurchases =
                            storage.getAllPurchases();

                    int totalRemoved = 0;
                    for (java.util.Map.Entry<java.util.UUID, java.util.Set<String>> entry : allPurchases.entrySet()) {
                        for (String effectName : entry.getValue()) {
                            if (!validEffects.contains(effectName.toLowerCase())) {
                                ps.setString(1, effectName);
                                totalRemoved += ps.executeUpdate();
                            }
                        }
                    }

                    if (totalRemoved > 0) {
                        plugin.getLogger().info("Cleaned up " + totalRemoved + " obsolete purchases");
                    }

                } catch (java.sql.SQLException e) {
                    throw e;
                }
            });

        } catch (Exception e) {
            plugin.getLogger().warning("Error cleaning up obsolete purchases: " + e.getMessage());
        }
    }

    private void updateEconomyReferences(EconomyManager newEconomyManager) {
        try {
            if (guiManager != null) {
                guiManager.updateEconomyManager(newEconomyManager);
            }
            if (plugin.getKtCommand() != null) {
                plugin.getKtCommand().updateEconomyManager(newEconomyManager);
            }
            if (plugin.getInventoryClickListener() != null) {
                plugin.getInventoryClickListener().updateEconomyManager(newEconomyManager);
            }
            if (plugin.getKillRewardListener() != null) {
                plugin.getKillRewardListener().updateEconomyManager(newEconomyManager);
            }
            if (plugin.getStatusLogger() != null) {
                plugin.getStatusLogger().updateEconomyManager(newEconomyManager);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating economy references: " + e.getMessage());
        }
    }

    private void reloadResourcePackForPlayers() {
        try {
            boolean resourcePackEnabled = plugin.getConfig().getBoolean("resource_pack.settings.enabled", true);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (resourcePackEnabled) {
                    plugin.getResourcePack().sendPackToPlayer(player);
                } else {
                    plugin.getResourcePack().removePackFromPlayer(player);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error reloading resource pack for players: " + e.getMessage());
        }
    }

    private String getEconomyProviderName(EconomyManager eco) {
        if (eco == null || !eco.isInitialized()) {
            return "Not Initialized";
        }

        if (eco.isUsingInternal()) {
            return "KillCoins (Internal)";
        } else if (eco.getVaultEconomy() != null) {
            return eco.getVaultEconomy().getName() + " (External)";
        } else {
            return "KillCoins (Fallback)";
        }
    }

    private boolean checkDatabaseConfigChanged() {
        DatabaseManager currentDb = plugin.getDatabaseManager();
        if (currentDb == null) return true;

        String newType = plugin.getConfig().getString("database.type", "sqlite");
        String newHost = plugin.getConfig().getString("database.mysql.host", "localhost");
        int newPort = plugin.getConfig().getInt("database.mysql.port", 3306);
        String newDatabase = plugin.getConfig().getString("database.mysql.database", "kt");
        String newUsername = plugin.getConfig().getString("database.mysql.username", "root");

        return !currentDb.isConnected() || isDifferentDatabaseConfig(newType, newHost, newPort, newDatabase, newUsername);
    }

    private boolean isDifferentDatabaseConfig(String type, String host, int port, String database, String username) {
        DatabaseManager currentDb = plugin.getDatabaseManager();

        if (!currentDb.isConnected()) {
            return true;
        }

        String currentType = currentDb.getDialect().name().toLowerCase();
        return !currentType.contains(type.toLowerCase());
    }

    private void reloadDatabaseManager() {
        try {
            plugin.getLogger().info("Database configuration changed, reloading database manager...");

            DatabaseManager currentDb = plugin.getDatabaseManager();
            if (currentDb != null) {
                currentDb.closeConnection();
            }

            DatabaseManager newDatabaseManager = new DatabaseManager(plugin);
            newDatabaseManager.loadDatabase();

            plugin.setDatabaseManager(newDatabaseManager);

            plugin.getLogger().info("Database manager reloaded successfully with " +
                    newDatabaseManager.getDialect().name() + " database");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload database manager: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database reload failed", e);
        }
    }
}