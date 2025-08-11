package com.monkey.kt.utils.discord.security;

import org.bukkit.entity.Player;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DailyRateLimiter {
    private static final Map<String, Long> playerCooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_24_HOURS = 24 * 60 * 60 * 1000L;

    public static boolean canUseCommand(Player player, String commandName) {
        String key = player.getUniqueId().toString() + "_" + commandName;
        Long lastUsed = playerCooldowns.get(key);

        if (lastUsed == null) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUsed) >= COOLDOWN_24_HOURS;
    }

    public static void setCommandUsed(Player player, String commandName) {
        String key = player.getUniqueId().toString() + "_" + commandName;
        playerCooldowns.put(key, System.currentTimeMillis());
    }

    public static long getRemainingCooldown(Player player, String commandName) {
        String key = player.getUniqueId().toString() + "_" + commandName;
        Long lastUsed = playerCooldowns.get(key);

        if (lastUsed == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUsed;
        return Math.max(0, COOLDOWN_24_HOURS - elapsed);
    }

    public static String formatTime(long milliseconds) {
        long hours = milliseconds / (60 * 60 * 1000);
        long minutes = (milliseconds % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (milliseconds % (60 * 1000)) / 1000;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    public static void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();
        playerCooldowns.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > (COOLDOWN_24_HOURS + 3600000)
        );
    }
}