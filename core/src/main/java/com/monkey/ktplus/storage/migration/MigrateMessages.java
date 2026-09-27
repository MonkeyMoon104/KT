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
        send(sender, "&eUsage:");
        send(sender, "&f/kt migrate <sqlite|mysql> <token>");
        send(sender, "&7Optional flags:");
        send(sender, "&f--dry-run");
        send(sender, "&f--include-temp-blocks");
        send(sender, "&f--force-pending-inventory");
        send(sender, "&f--allow-nonempty-target");
        send(sender, "&7Tip: run without &f<token> &7to get a copyable one.");
    }

    public static void noPermission(CommandSender sender, String localized) {
        send(sender, "&c" + stripLeadingColors(localized));
    }

    public static void invalidArgs(CommandSender sender) {
        send(sender, "&cInvalid target. Use &fsqlite &cor &fmysql&c.");
        usage(sender);
    }

    public static void starting(CommandSender sender, MigrationDialect target, boolean dryRun) {
        if (dryRun) {
            send(sender, "&aDry-run → &f" + target.configValue());
        } else {
            send(sender, "&aMigrating → &f" + target.configValue());
        }
    }

    public static void showOutcome(CommandSender sender, MigrationOutcome outcome) {
        MigrationContext context = outcome.context();
        MigrationEndpoint source = context.sourceEndpoint();
        MigrationEndpoint target = context.targetEndpoint();

        if (source != null) {
            send(sender, "&7From &f" + source.engineLabel() + " &8· &f" + source.chatLabel());
        }
        if (target != null) {
            String engine = targetEngineLabel(context, target);
            send(sender, "&7To   &f" + engine + " &8· &f" + target.chatLabel());
        }

        boolean confirmationMismatch = isConfirmationMismatch(outcome.message());
        if (confirmationMismatch && target != null) {
            send(sender, "&eClick token to copy, then re-run:");
            sendCopyableToken(sender, target.confirmationToken());
            sendSuggestRerun(sender, target);
            send(sender, "&cWrong or missing token.");
            sendReport(sender, outcome.reportPath());
            return;
        }

        for (String warning : context.warnings()) {
            send(sender, "&e⚠ &7" + warning);
        }

        if (outcome.success()) {
            if (context.request().dryRun()) {
                send(sender, "&aDry-run OK. No data written.");
            } else {
                send(sender, "&aMigration OK.");
                if (context.configSwapped()) {
                    String type = context.targetMariaDb() ? "mariadb" : context.request().targetDialect().configValue();
                    send(sender, "&7database.type → &f" + type);
                    send(sender, "&eRestart the server to finish.");
                }
            }
        } else {
            sendFailure(sender, outcome.message());
        }

        sendReport(sender, outcome.reportPath());
    }

    public static void failed(CommandSender sender, String detail) {
        sendFailure(sender, detail);
    }

    private static void sendFailure(CommandSender sender, String message) {
        send(sender, "&cMigration failed.");
        for (String line : humanizeFailureLines(message)) {
            send(sender, "&7" + line);
        }
    }

    private static void sendReport(CommandSender sender, Path report) {
        if (report == null) {
            return;
        }
        String name = report.getFileName() == null ? report.toString() : report.getFileName().toString();
        String full = report.toString().replace('\\', '/');
        if (sender instanceof Player player) {
            Component message = TextFormatter.component(PREFIX + "&7Report: ")
                    .append(Component.text(name)
                            .color(NamedTextColor.AQUA)
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.copyToClipboard(full))
                            .hoverEvent(HoverEvent.showText(
                                    Component.text("Click to copy path").color(NamedTextColor.GRAY))));
            player.sendMessage(message);
        } else {
            send(sender, "&7Report: &f" + name);
        }
    }

    private static void sendSuggestRerun(CommandSender sender, MigrationEndpoint target) {
        String command = "/kt migrate " + target.dialect().configValue() + " " + target.confirmationToken();
        if (sender instanceof Player player) {
            Component message = TextFormatter.component(PREFIX + "&aClick here &7to paste command.")
                    .clickEvent(ClickEvent.suggestCommand(command))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Paste into chat").color(NamedTextColor.GRAY)));
            player.sendMessage(message);
        } else {
            send(sender, "&7Command: &f" + command);
        }
    }

    private static void sendCopyableToken(CommandSender sender, String token) {
        if (sender instanceof Player player) {
            Component message = TextFormatter.component(PREFIX + "&7Token: ")
                    .append(Component.text(token)
                            .color(NamedTextColor.AQUA)
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.copyToClipboard(token))
                            .hoverEvent(HoverEvent.showText(
                                    Component.text("Click to copy").color(NamedTextColor.GRAY))));
            player.sendMessage(message);
        } else {
            send(sender, "&7Token: &b" + token);
        }
    }

    private static String targetEngineLabel(MigrationContext context, MigrationEndpoint target) {
        if (target.dialect() == MigrationDialect.SQLITE) {
            return "SQLite";
        }
        if (context.targetMariaDb()) {
            return "MariaDB";
        }
        return "MySQL";
    }

    private static boolean isConfirmationMismatch(String message) {
        String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
        return lower.contains("confirmation mismatch") || lower.contains("target confirmation");
    }

    private static String[] humanizeFailureLines(String message) {
        if (message == null || message.isBlank()) {
            return new String[] {"Unknown error."};
        }
        String raw = message.trim();
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("confirmation mismatch")) {
            return new String[] {"Token did not match the target."};
        }
        if (lower.contains("target-nonempty") || lower.contains("already contains kt_")) {
            return new String[] {
                "Target DB is not empty.",
                "Add flag: --allow-nonempty-target"
            };
        }
        if (lower.contains("pending-inventory") || lower.contains("pending inventory")) {
            return new String[] {
                "Pending GUI items on source.",
                "Close GUIs, or add: --force-pending-inventory"
            };
        }
        if (lower.contains("mariadb") && lower.contains("required")) {
            return new String[] {"MariaDB is too old (need 10.5+)."};
        }
        if (lower.contains("mysql") && lower.contains("required")) {
            return new String[] {"MySQL is too old (need 8.0+)."};
        }
        if (lower.contains("utf8mb4")) {
            return new String[] {"Charset must be utf8mb4."};
        }
        if (lower.startsWith("preflight failed:")) {
            return new String[] {raw.substring("preflight failed:".length()).trim()};
        }
        return new String[] {raw};
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
