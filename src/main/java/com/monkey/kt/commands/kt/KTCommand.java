package com.monkey.kt.commands.kt;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.manager.KTCManager;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KTCommand implements CommandExecutor {

    private final KTCManager manager;
    private final GUIManager guiManager;
    private final KT plugin;

    public KTCommand(KT plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.manager = new KTCManager(plugin, guiManager);
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (sender instanceof Player player) {
                guiManager.openGUI(player);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.only_players")));
            }
            return true;
        }

        SubCommand sub = manager.getSubCommand(args[0]);
        if (sub != null) {
            if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no_permissions")));
                return true;
            }
            sub.execute(sender, args);
        }
        return true;
    }
}
