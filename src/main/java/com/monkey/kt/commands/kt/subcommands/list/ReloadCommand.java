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

        if (plugin.getEffectRegistry() != null) {
            plugin.getEffectRegistry().loadEffects();
        }

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