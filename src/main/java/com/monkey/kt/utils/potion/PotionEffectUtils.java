package com.monkey.kt.utils.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public final class PotionEffectUtils {

    private PotionEffectUtils() {
    }

    public static PotionEffectType fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String normalized = name.toLowerCase(Locale.ROOT);
        NamespacedKey key = normalized.contains(":")
                ? NamespacedKey.fromString(normalized)
                : NamespacedKey.minecraft(normalized);

        return key != null ? Registry.EFFECT.get(key) : null;
    }

    public static String displayName(PotionEffectType type) {
        if (type == null) {
            return "unknown";
        }

        NamespacedKey key = Registry.EFFECT.getKey(type);
        String value = key != null ? key.getKey() : "unknown";
        String[] parts = value.split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(parts[i].charAt(0)))
                    .append(parts[i].substring(1).toLowerCase(Locale.ROOT));
        }

        return builder.toString();
    }
}
