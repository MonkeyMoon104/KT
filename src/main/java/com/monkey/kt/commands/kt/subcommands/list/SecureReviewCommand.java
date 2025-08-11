package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.utils.discord.WebhookManager;
import com.monkey.kt.utils.discord.security.AntiAbuseSystem;
import com.monkey.kt.utils.discord.security.CommandLogger;
import com.monkey.kt.utils.discord.security.DailyRateLimiter;
import com.monkey.kt.utils.discord.security.WebhookDecryptor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SecureReviewCommand implements SubCommand {
    private final KT plugin;
    private WebhookManager webhookManager;

    public SecureReviewCommand(KT plugin) {
        this.plugin = plugin;

        CommandLogger.initialize(plugin.getDataFolder());

        String webhookUrl = WebhookDecryptor.getReviewWebhook();
        if (webhookUrl != null) {
            this.webhookManager = new WebhookManager(webhookUrl, plugin);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                DailyRateLimiter.cleanupOldEntries();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 3600, 20L * 3600);
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
        if (webhookManager == null || !plugin.getConfig().getBoolean("management-structure.review-command.enabled")) {
            sender.sendMessage(color("&cService temporarily unavailable."));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cOnly players can use this command."));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(getPermission())) {
            sender.sendMessage(color("&cYou don't have permission to use this command."));
            return;
        }

        if (!AntiAbuseSystem.checkAttempt(player, "review")) {
            sender.sendMessage(color("&cYou're making too many attempts. Please wait before trying again."));
            return;
        }

        if (!DailyRateLimiter.canUseCommand(player, "review")) {
            long remaining = DailyRateLimiter.getRemainingCooldown(player, "review");
            String timeLeft = DailyRateLimiter.formatTime(remaining);

            sender.sendMessage(color("&cYou can only review once every 24 hours!"));
            sender.sendMessage(color("&cTime remaining: &f" + timeLeft));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(color("&cUsage: /kt review <1-5>"));
            return;
        }

        int stars;
        try {
            stars = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(color("&cPlease provide a valid number (1-5)."));
            return;
        }

        if (stars < 1 || stars > 5) {
            sender.sendMessage(color("&cRating must be between 1 and 5 stars."));
            return;
        }

        String optionalComment = "";
        if (args.length > 2) {
            optionalComment = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        }

        DailyRateLimiter.setCommandUsed(player, "review");

        CommandLogger.logCommandUsage(player, "review", stars + " stars" + (optionalComment.isEmpty() ? "" : " - " + optionalComment));

        String finalComment = optionalComment;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    webhookManager.sendReview(player.getName(), stars, finalComment);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(color("&a✓ Thanks for your " + stars + " star review! ❤"));

                            if (stars >= 4) {
                                player.sendMessage(color("&7Your positive feedback helps us improve!"));
                            }
                        }
                    }.runTask(plugin);

                } catch (Exception e) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(color("&cFailed to send review. Please try again later."));
                        }
                    }.runTask(plugin);

                    plugin.getLogger().severe("Failed to send review webhook: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);

        if (CommandLogger.getCommandUsageToday("review") % 10 == 0) {
            int todayCount = CommandLogger.getCommandUsageToday("review");
            plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("kt.admin.stats"))
                    .forEach(admin -> admin.sendMessage(
                            color("&a[KT-STATS] &f" + todayCount + " reviews received today!")));
        }
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}