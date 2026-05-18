package com.monkey.kt.effects.permission;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class EffectPermissionResolver {

    private EffectPermissionResolver() {
    }

    public static String resolvePermission(KT plugin, String effectId) {
        if (effectId == null || effectId.trim().isEmpty()) {
            return "";
        }

        String normalized = plugin.resolveEffectId(normalize(effectId));
        String overridePermission = resolveOverridePermission(plugin, normalized);
        if (isUsablePermission(overridePermission)) {
            return overridePermission.trim();
        }

        if (plugin.getCustomEffectLoader() != null) {
            CustomEffectConfig custom = plugin.getCustomEffectLoader().getEffectConfig(normalized);
            if (custom != null && isUsablePermission(custom.getPermission())) {
                return custom.getPermission().trim();
            }
        }

        String template = plugin.getConfig()
                .getString("custom-effects.permissions.default-node", "kt.%effect%.use");

        if (!isUsablePermission(template)) {
            return "";
        }

        return template.replace("%effect%", normalized).trim();
    }

    public static boolean hasExplicitPermissionRule(KT plugin, String effectId) {
        if (effectId == null || effectId.trim().isEmpty()) {
            return false;
        }

        String normalized = plugin.resolveEffectId(normalize(effectId));
        String overridePermission = resolveOverridePermission(plugin, normalized);
        if (isUsablePermission(overridePermission)) {
            return true;
        }

        if (plugin.getCustomEffectLoader() == null) {
            return false;
        }

        CustomEffectConfig custom = plugin.getCustomEffectLoader().getEffectConfig(normalized);
        return custom != null && isUsablePermission(custom.getPermission());
    }

    public static boolean hasPermission(Player player, KT plugin, String effectId) {
        String permission = resolvePermission(plugin, effectId);
        if (!isUsablePermission(permission)) {
            return true;
        }
        return player.hasPermission(permission);
    }

    public static String normalize(String effectId) {
        return effectId.toLowerCase(Locale.ROOT).trim();
    }

    private static String resolveOverridePermission(KT plugin, String effectId) {
        for (String acceptedId : plugin.getAcceptedEffectIds(effectId)) {
            String overridePermission = plugin.getConfig()
                    .getString("custom-effects.permissions.overrides." + acceptedId, "");
            if (isUsablePermission(overridePermission)) {
                return overridePermission;
            }
        }
        return "";
    }

    private static boolean isUsablePermission(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return false;
        }
        return !normalized.equalsIgnoreCase("none");
    }
}
