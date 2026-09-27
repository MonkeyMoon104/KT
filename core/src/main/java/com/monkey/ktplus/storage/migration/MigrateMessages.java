package com.monkey.ktplus.storage.migration;

import com.monkey.ktplus.storage.migration.connection.MigrationEndpoint;
import com.monkey.ktplus.util.text.TextFormatter;
import java.nio.file.Path;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MigrateMessages {
    private static final String PREFIX = "&8[&bKT+&8] &7";

    private MigrateMessages() {}

    public static void usage(CommandSender sender) {
        send(sender, "&eUsage: &f/kt migrate <sqlite|mysql> [flags] <token>");
        send(sender, "&7Flags: &f--dry-run &7&f--include-temp-blocks &7&f--force-pending-inventory &7&f--allow-nonempty-target");
        send(sender, "&7Run once without a token to see source, target, and a short copyable confirm token.");
    }

    public static void noPermission(CommandSender sender, String localized) {
        send(sender, "&c" + stripLeadingColors(localized));
    }

    public static void invalidArgs(CommandSender sender) {
        send(sender, "&cInvalid migrate args. Target must be &fsqlite &cor &fmysql&c.");
        usage(sender);
    }

    public static void starting(CommandSender sender, MigrationDialect target, boolean dryRun) {
        String mode = dryRun ? "dry-run " : "";
        send(sender, "&aStarting " + mode + "migration to &f" + target.configValue() + "&a…");
        send(sender, "&7Writes may freeze briefly. Confirm the target with the token below.");
    }

    public static void showOutcome(CommandSender sender, MigrationOutcome outcome) {
        MigrationContext context = outcome.context();
        MigrationEndpoint source = context.sourceEndpoint();
        MigrationEndpoint target = context.targetEndpoint();

        if (source != null) {
            send(sender, "&7Source: &f" + source.displaySummary());
        }
        if (target != null) {
            send(sender, "&7Target: &f" + target.displaySummary());
        }

        boolean confirmationMismatch = isConfirmationMismatch(outcome.message());
        if (confirmationMismatch && target != null) {
            send(sender, "&eConfirm the target — click the token to copy, then re-run:");
            sendCopyableToken(sender, target.confirmationToken());
            sendSuggestRerun(sender, target);
        }

        for (String warning : context.warnings()) {
            send(sender, "&eWarning: &7" + warning);
        }

        if (outcome.success()) {
            send(sender, "&aMigration OK&7 — " + outcome.message() + "&a.");
        } else if (!confirmationMismatch) {
            send(sender, "&cMigration failed&7 — " + humanizeFailure(outcome.message()));
        } else {
            send(sender, "&cToken mismatch. Paste the token above as the last argument.");
        }

        Path report = outcome.reportPath();
        if (report != null) {
            send(sender, "&7Report: &f" + report);
        }
    }

    public static void failed(CommandSender sender, String detail) {
        send(sender, "&cMigration failed&7 — " + humanizeFailure(detail));
    }

    private static void sendSuggestRerun(CommandSender sender, MigrationEndpoint target) {
        String command = "/kt migrate " + target.dialect().configValue() + " " + target.confirmationToken();
        if (sender instanceof Player player) {
            Component message = TextFormatter.component(PREFIX + "&aClick here &7to paste the full command.")
                    .clickEvent(ClickEvent.suggestCommand(command))
                    .hoverEvent(HoverEvent.showText(Component.text("Paste command into chat")
                            .color(NamedTextColor.GRAY)));
            player.sendMessage(message);
        } else {
            send(sender, "&7Re-run: &f" + command);
        }
    }

    private static void sendCopyableToken(CommandSender sender, String token) {
        if (sender instanceof Player player) {
            Component message = TextFormatter.component(PREFIX + "&7Confirm token: ")
                    .append(Component.text(token)
                            .color(NamedTextColor.AQUA)
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.copyToClipboard(token))
                            .hoverEvent(HoverEvent.showText(
                                    Component.text("Click to copy").color(NamedTextColor.GRAY))));
            player.sendMessage(message);
        } else {
            send(sender, "&7Confirm token: &b" + token);
        }
    }

    private static boolean isConfirmationMismatch(String message) {
        String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
        return lower.contains("confirmation mismatch") || lower.contains("target confirmation");
    }

    private static String humanizeFailure(String message) {
        if (message == null || message.isBlank()) {
            return "unknown error";
        }
        String trimmed = message.trim();
        if (trimmed.startsWith("target confirmation mismatch")) {
            return "confirmation token did not match the target";
        }
        return trimmed;
    }

    private static String stripLeadingColors(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceFirst("^(§[0-9a-fk-or])*", "").replaceFirst("^(&[0-9a-fk-or])*", "");
    }

    private static void send(CommandSender sender, String message) {
        sender.sendMessage(TextFormatter.color(PREFIX + message));
    }
}
