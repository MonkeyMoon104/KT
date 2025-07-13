package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.config_reloaded")));
    }
}
