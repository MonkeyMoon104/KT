package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.cooldown.CommandCooldownManager;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.KillEffectFactory;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class TestCommand implements SubCommand {

    private final KT plugin;

    public TestCommand(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getPermission() {
        return "kt.test";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.only_players")));
            return;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.miss_usage")));
            return;
        }

        String effectName = args[1].toLowerCase();
        KillEffect effect = KillEffectFactory.getEffect(effectName);
        if (effect == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.effect_not_found")));
            return;
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("management-structure.test_command.cooldown");
        if (section != null && section.getBoolean("enabled")) {
            if (!player.hasPermission("kt.admin.bypass")) {
                String cooldownKey = "test_command";
                if (CommandCooldownManager.isOnCooldown(player, cooldownKey)) {
                    long remaining = CommandCooldownManager.getRemaining(player, cooldownKey);
                    String msg = plugin.getConfig().getString("messages.on_command_cooldown", "&cPlease wait %seconds%s before using this command again.")
                            .replace("%seconds%", String.valueOf(remaining));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    return;
                }

                long delay = section.getLong("delay", 10);
                CommandCooldownManager.setCooldown(player, cooldownKey, delay);
            }
        }

        effect.play(player, player.getLocation());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.effect_executed"))
                .replace("%effect%", effectName));
    }
}
