package com.monkey.kt.utils.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

public final class TextUtils {

    private static final LegacyComponentSerializer LEGACY_AMPERSAND =
            LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION =
            LegacyComponentSerializer.legacySection();

    private TextUtils() {
    }

    public static Component component(String text) {
        return LEGACY_AMPERSAND.deserialize(Objects.toString(text, ""));
    }

    public static List<Component> components(List<String> lines) {
        return lines.stream()
                .map(TextUtils::component)
                .toList();
    }

    public static void send(CommandSender sender, String text) {
        sender.sendMessage(component(text));
    }

    public static void broadcast(Server server, String text) {
        Component message = component(text);
        server.getConsoleSender().sendMessage(message);
        server.getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }

    public static String legacySection(String text) {
        return LEGACY_SECTION.serialize(component(text));
    }
}
