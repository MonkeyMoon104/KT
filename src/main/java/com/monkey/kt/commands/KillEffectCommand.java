package com.monkey.kt.commands;

import com.monkey.kt.KT;
import com.monkey.kt.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillEffectCommand implements CommandExecutor {

    private final KT plugin;
    private GUIManager guiManager;

    public KillEffectCommand(KT plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("kt.reload")) {
                sender.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.no_permissions"));
                return true;
            }

            plugin.reloadConfig();
            this.guiManager = new GUIManager(plugin, plugin.getDatabaseManager());
            sender.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("messages.config_reloaded"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        guiManager.openGUI(player);
        return true;
    }
}
