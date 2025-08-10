package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.cooldown.CommandCooldownManager;
import com.monkey.kt.utils.discord.WebhookManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReviewCommand implements SubCommand {

    private final KT plugin;
    private final WebhookManager webhookManager;

    public ReviewCommand(KT plugin) {
        this.plugin = plugin;
        this.webhookManager = new WebhookManager("https://discord.com/api/webhooks/enter your url here", plugin);
    }

    @Override
    public String getName() {
        return "review";
    }

    @Override
    public String getPermission() {
        return "kt.user.review";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("management-structure.review-command.enabled", true)) {
            sender.sendMessage(color(plugin.getConfig().getString("management-structure.review-command.messages.command-disabled")));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(color(plugin.getConfig().getString("management-structure.review-command.messages.only-players")));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(getPermission())) {
            player.sendMessage(color(plugin.getConfig().getString("management-structure.review-command.messages.no-permission")));
            return;
        }

        if (!player.hasPermission("kt.admin.bypass")) {
            String cooldownKey = "review_command";
            if (CommandCooldownManager.isOnCooldown(player, cooldownKey)) {
                long remaining = CommandCooldownManager.getRemaining(player, cooldownKey);
                long hours = remaining / 3600;
                long minutes = (remaining % 3600) / 60;
                long seconds = remaining % 60;

                String timeFormat;
                if (hours > 0) {
                    timeFormat = hours + "h " + minutes + "m " + seconds + "s";
                } else if (minutes > 0) {
                    timeFormat = minutes + "m " + seconds + "s";
                } else {
                    timeFormat = seconds + "s";
                }

                String msg = color(plugin.getConfig().getString("management-structure.review-command.messages.cooldown",
                        "&cYou can only review once every 24 hours! Please wait %time% before using this command again."))
                        .replace("%time%", timeFormat);
                player.sendMessage(msg);
                return;
            }
        }

        if (args.length != 2) {
            player.sendMessage(color(plugin.getConfig().getString("management-structure.review-command.messages.usage")));
            return;
        }

        int stars;
        try {
            stars = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            player.sendMessage(color(plugin.getConfig().getString("management-structure.review-command.messages.not-a-number")));
            return;
        }

        if (stars < 1 || stars > 5) {
            player.sendMessage(color(plugin.getConfig().getString("management-structure.review-command.messages.out-of-range")));
            return;
        }

        webhookManager.sendReview(player.getName(), stars);

        if (!player.hasPermission("kt.admin.bypass")) {
            CommandCooldownManager.setCooldown(player, "review_command", 86400);
        }

        player.sendMessage(color(plugin.getConfig().getString("management-structure.review-command.messages.success")
                .replace("%stars%", String.valueOf(stars))));
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}