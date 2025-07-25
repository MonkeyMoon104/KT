package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.economy.KillCoinsEco;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KillCoinsCommand implements SubCommand, TabCompleter {

    private final KT plugin;
    private final KillCoinsEco eco;

    private static final List<String> ACTIONS = Arrays.asList("add", "take", "set", "reset", "bal");

    public KillCoinsCommand(KT plugin, KillCoinsEco eco) {
        this.plugin = plugin;
        this.eco = eco;
    }

    @Override
    public String getName() {
        return "killcoins";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        String action = args[1].toLowerCase();

        if ("bal".equals(action)) {
            if (!sender.hasPermission("kt.killcoins.bal")) {
                sender.sendMessage(color(plugin.getConfig().getString("messages.commands_no_perm")));
                return;
            }
            if (args.length == 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_only_players")));
                    return;
                }
                Player player = (Player) sender;
                double balance = eco.getBalance(player);
                sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_balance_self")
                        .replace("%balance%", String.valueOf((int) balance))
                        .replace("%currency%", eco.currencySymbol())));
                return;
            } else if (args.length == 3) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                double balance = eco.getBalance(target);
                sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_balance_other")
                        .replace("%player%", target.getName())
                        .replace("%balance%", String.valueOf((int) balance))
                        .replace("%currency%", eco.currencySymbol())));
                return;
            } else {
                sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_balance_usage")));
                return;
            }
        }

        if ("reset".equals(action)) {
            if (!sender.hasPermission("kt.killcoins.reset")) {
                sender.sendMessage(color(plugin.getConfig().getString("messages.commands_no_perm")));
                return;
            }
            if (args.length != 3) {
                sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_reset_usage")));
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            double start = plugin.getConfig().getDouble("economy.starting-balance", 0D);
            eco.setBalance(target, start);
            sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_reset")
                    .replace("%player%", target.getName())
                    .replace("%balance%", String.valueOf((int) start))
                    .replace("%currency%", eco.currencySymbol())));
            return;
        }

        if (args.length != 4) {
            sendUsage(sender);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_invalid_amount")
                    .replace("%amount%", args[3])));
            return;
        }

        switch (action) {
            case "add":
                if (!sender.hasPermission("kt.killcoins.add")) {
                    sender.sendMessage(color(plugin.getConfig().getString("messages.commands_no_perm")));
                    return;
                }
                eco.add(target, amount);
                sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_add")
                        .replace("%amount%", String.valueOf((int) amount))
                        .replace("%player%", target.getName())
                        .replace("%currency%", eco.currencySymbol())));
                break;

            case "take":
                if (!sender.hasPermission("kt.killcoins.take")) {
                    sender.sendMessage(color(plugin.getConfig().getString("messages.commands_no_perm")));
                    return;
                }
                boolean success = eco.withdraw(target, amount);
                if (success) {
                    sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_take")
                            .replace("%amount%", String.valueOf((int) amount))
                            .replace("%player%", target.getName())
                            .replace("%currency%", eco.currencySymbol())));
                } else {
                    sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_take_not_enough")
                            .replace("%player%", target.getName())
                            .replace("%currency%", eco.currencySymbol())));
                }
                break;

            case "set":
                if (!sender.hasPermission("kt.killcoins.set")) {
                    sender.sendMessage(color(plugin.getConfig().getString("messages.commands_no_perm")));
                    return;
                }
                eco.setBalance(target, amount);
                sender.sendMessage(color(plugin.getConfig().getString("messages.killcoins_set")
                        .replace("%amount%", String.valueOf((int) amount))
                        .replace("%player%", target.getName())
                        .replace("%currency%", eco.currencySymbol())));
                break;

            default:
                sendUsage(sender);
        }
    }

    private void sendUsage(CommandSender sender) {
        String[] usageLines = plugin.getConfig().getString("messages.killcoins_usage").split("\n");
        for (String line : usageLines) {
            sender.sendMessage(color(line));
        }
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            for (String a : ACTIONS) {
                if (a.startsWith(args[1].toLowerCase())) {
                    completions.add(a);
                }
            }
        } else if (args.length == 3) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(p.getName());
                }
            }
        }

        Collections.sort(completions);
        return completions;
    }
}
