package com.monkey.kt.commands.kt.tab;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.list.KillCoinsCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KillEffectTabCompleter implements TabCompleter {

    private final KT plugin;
    private final KillCoinsCommand killCoinsCommand;

    public KillEffectTabCompleter(KT plugin, KillCoinsCommand killCoinsCommand) {
        this.plugin = plugin;
        this.killCoinsCommand = killCoinsCommand;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("killcoins")) {
            return killCoinsCommand.onTabComplete(sender, command, alias, args);
        }

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "set", "test", "killcoins", "review");

            if (sender instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
                if (player.isOp()) {
                    subcommands = Arrays.asList("reload", "set", "test", "killcoins", "suggestion", "review");
                }
            }

            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("test"))) {
            List<String> effects = plugin.getGuiManager().getEffects().keySet().stream()
                    .filter(effect -> {
                        if (!(sender instanceof org.bukkit.entity.Player)) return true;
                        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;

                        if (player.isOp()) return true;

                        boolean hasPermission = player.hasPermission("kt." + effect + ".use");
                        boolean hasBought = plugin.getKillCoinsEco().hasBoughtEffect(player, effect);
                        boolean ecoEnabled = plugin.getKillCoinsEco().isEnabled();

                        return hasPermission || (ecoEnabled && hasBought);
                    })
                    .collect(Collectors.toList());

            effects.add("none");

            return effects.stream()
                    .filter(effect -> effect.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
