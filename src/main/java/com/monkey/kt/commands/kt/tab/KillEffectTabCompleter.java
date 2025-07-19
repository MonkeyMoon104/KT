package com.monkey.kt.commands.kt.tab;

import com.monkey.kt.KT;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillEffectTabCompleter implements TabCompleter {

    private final KT plugin;

    public KillEffectTabCompleter(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "set", "test");
            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("test"))) {
            return plugin.getGuiManager().getEffects().keySet().stream()
                    .filter(effect -> effect.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}