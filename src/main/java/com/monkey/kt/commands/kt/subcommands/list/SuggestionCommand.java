package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.utils.discord.WebhookManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuggestionCommand implements SubCommand {

    private final KT plugin;
    private final WebhookManager webhookManager;

    public SuggestionCommand(KT plugin) {
        this.plugin = plugin;
        this.webhookManager = new WebhookManager("https://discord.com/api/webhooks/enter your url here", plugin);
    }

    @Override
    public String getName() {
        return "suggestion";
    }

    @Override
    public String getPermission() {
        return "kt.admin.suggestion";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.only_players")));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(getPermission())) {
            player.sendMessage(color(plugin.getConfig().getString("messages.commands_no_perm")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Correct usage: /kt suggestion <true|false> <text>");
            return;
        }

        String firstArg = args[1].toLowerCase();
        if (!firstArg.equals("true") && !firstArg.equals("false")) {
            player.sendMessage(ChatColor.RED + "First param must be 'true' (bug) or 'false' (suggestion)!");
            return;
        }

        boolean isBug = Boolean.parseBoolean(firstArg);
        String suggestion = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        webhookManager.sendSuggestion(player.getName(), suggestion, isBug);

        player.sendMessage(ChatColor.GREEN + (isBug
                ? "Your bug suggestion has been sent to MonkeyMoon104, thank you ❤"
                : "Your suggestion has been sent to MonkeyMoon104, thank you ❤"));
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}