package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.KTCommand;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.gui.GUIManager;
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

        boolean wasUsingInternal = plugin.getEconomyManager().isUsingInternal();
        String previousProvider = getEconomyProviderName(plugin.getEconomyManager());

        plugin.reloadConfig();

        plugin.getEffectRegistry().loadEffects();

        boolean newUseInternal = plugin.getConfig().getBoolean("economy.use_internal", true);

        if (wasUsingInternal != newUseInternal) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&6Economy type changed from " + (wasUsingInternal ? "Internal" : "External") +
                            " to " + (newUseInternal ? "Internal" : "External") + "!"));

            EconomyManager newEconomyManager = new EconomyManager(plugin, plugin.getKillCoinsEco());

            plugin.setEconomyManager(newEconomyManager);

            guiManager.updateEconomyManager(newEconomyManager);
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

            String newProvider = getEconomyProviderName(newEconomyManager);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&aEconomy provider changed from &e" + previousProvider + " &ato &e" + newProvider));

            plugin.getLogger().info("Economy type changed from " + previousProvider + " to " + newProvider);
        } else {
            String newProvider = getEconomyProviderName(plugin.getEconomyManager());
            if (!previousProvider.equals(newProvider)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&6Economy provider changed from &e" + previousProvider + " &ato &e" + newProvider));
            }
        }

        if (plugin.getEffectRegistry() != null) {
            plugin.getEffectRegistry().loadEffects();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getConfig().getBoolean("resource_pack.settings.enabled", true)) {
                plugin.getResourcePack().sendPackToPlayer(player);
            } else {
                plugin.getResourcePack().removePackFromPlayer(player);
            }
        }

        plugin.getStatusLogger().logReload();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.config_reloaded", "&aConfiguration reloaded successfully!")));
    }

    private String getEconomyProviderName(EconomyManager eco) {
        if (eco.isUsingInternal()) {
            return "KillCoins (Internal)";
        } else if (eco.getVaultEconomy() != null) {
            return eco.getVaultEconomy().getName() + " (External)";
        } else {
            return "KillCoins (Fallback)";
        }
    }
}
