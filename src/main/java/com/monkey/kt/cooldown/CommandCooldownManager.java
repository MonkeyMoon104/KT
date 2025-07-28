package com.monkey.kt.cooldown;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandCooldownManager {

    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public static boolean isOnCooldown(Player player, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;

        Long expiresAt = playerCooldowns.get(key);
        return expiresAt != null && expiresAt > System.currentTimeMillis();
    }

    public static void setCooldown(Player player, String key, long delaySeconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>())
                .put(key, System.currentTimeMillis() + (delaySeconds * 1000));
    }

    public static long getRemaining(Player player, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;

        Long expiresAt = playerCooldowns.get(key);
        if (expiresAt == null) return 0;

        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(remaining / 1000, 0);
    }
}
