package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.utils.discord.WebhookManager;
import com.monkey.kt.utils.discord.security.AntiAbuseSystem;
import com.monkey.kt.utils.discord.security.CommandLogger;
import com.monkey.kt.utils.discord.security.DailyRateLimiter;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class SecureSuggestionCommand implements SubCommand {
    private final KT plugin;
    private final WebhookManager webhookManager;

    public SecureSuggestionCommand(KT plugin) {
        this.plugin = plugin;
        this.webhookManager = new WebhookManager(plugin);
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
            sender.sendMessage(color("&cOnly players can use this command."));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(getPermission())) {
            sender.sendMessage(color("&cYou don't have permission to use this command."));
            return;
        }

        if (!AntiAbuseSystem.checkAttempt(player, "suggestion")) {
            sender.sendMessage(color("&cYou're making too many attempts. Please wait before trying again."));
            return;
        }

        if (!DailyRateLimiter.canUseCommand(player, "suggestion")) {
            long remaining = DailyRateLimiter.getRemainingCooldown(player, "suggestion");
            String timeLeft = DailyRateLimiter.formatTime(remaining);

            sender.sendMessage(color("&cYou can only suggest once every 24 hours!"));
            sender.sendMessage(color("&cTime remaining: &f" + timeLeft));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(color("&cUsage: /kt suggestion <true|false> <text>"));
            sender.sendMessage(color("&7Use 'true' for bugs, 'false' for suggestions"));
            return;
        }

        String firstArg = args[1].toLowerCase();
        if (!firstArg.equals("true") && !firstArg.equals("false")) {
            sender.sendMessage(color("&cFirst parameter must be 'true' (bug) or 'false' (suggestion)!"));
            return;
        }

        boolean isBug = Boolean.parseBoolean(firstArg);
        String suggestion = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        if (suggestion.trim().length() < 10) {
            sender.sendMessage(color("&cSuggestion must be at least 10 characters long."));
            return;
        }

        DailyRateLimiter.setCommandUsed(player, "suggestion");

        CommandLogger.logCommandUsage(player, "suggestion",
                (isBug ? "BUG" : "SUGGESTION") + ": " + suggestion.substring(0, Math.min(50, suggestion.length())));

        SchedulerWrapper.runTaskAsynchronously(plugin, () -> {
            try {
                webhookManager.sendSuggestion(player.getName(), suggestion, isBug);

                SchedulerWrapper.runTask(plugin, () -> {
                        player.sendMessage(color("&a✓ Your " + (isBug ? "bug report" : "suggestion") +
                                " has been sent to MonkeyMoon104, thank you ❤"));
                    });

            } catch (Exception e) {
                SchedulerWrapper.runTask(plugin, () -> {
                    player.sendMessage(color("&cFailed to send " + (isBug ? "bug report" : "suggestion") +
                            ". Please try again later."));
                    });

                plugin.getLogger().severe("Failed to send suggestion webhook: " + e.getMessage());
            }
        });
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}