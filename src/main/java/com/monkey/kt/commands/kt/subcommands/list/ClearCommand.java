package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.storage.EffectStorage;
import com.monkey.kt.utils.text.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearCommand implements SubCommand {

    private final KT plugin;

    public ClearCommand(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getPermission() {
        return "kt.clear";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.only_players")));
            return;
        }

        Player player = (Player) sender;
        String current = EffectStorage.getEffect(player);

        if (current == null) {
            player.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.effect_none_selected")));
            return;
        }

        EffectStorage.removeEffect(player);
        player.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.effect_removed")));
    }
}
