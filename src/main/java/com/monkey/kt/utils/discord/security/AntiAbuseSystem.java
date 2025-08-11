package com.monkey.kt.utils.discord.security;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class AntiAbuseSystem {
    private static final Map<String, Integer> attemptCounts = new ConcurrentHashMap<>();
    private static final Set<String> suspiciousPlayers = new HashSet<>();
    private static final int MAX_ATTEMPTS_PER_HOUR = 5;
    private static final long HOUR_IN_MILLIS = 60 * 60 * 1000L;

    public static boolean checkAttempt(Player player, String command) {
        String playerKey = player.getUniqueId().toString() + "_" + command;
        long currentTime = System.currentTimeMillis();

        String hourKey = playerKey + "_" + (currentTime / HOUR_IN_MILLIS);
        Integer attempts = attemptCounts.get(hourKey);

        if (attempts == null) {
            attempts = 0;
            attemptCounts.entrySet().removeIf(entry ->
                    !entry.getKey().endsWith("_" + (currentTime / HOUR_IN_MILLIS)));
        }

        attempts++;
        attemptCounts.put(hourKey, attempts);

        if (attempts > MAX_ATTEMPTS_PER_HOUR) {
            suspiciousPlayers.add(player.getUniqueId().toString());

            Bukkit.getLogger().warning("Player " + player.getName() +
                    " is attempting to spam " + command + " command (" + attempts + " attempts)");

            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("kt.admin.notifications"))
                    .forEach(admin -> admin.sendMessage("§c[ANTI-ABUSE] §f" + player.getName() +
                            " is spamming " + command + " command!"));

            return false;
        }

        return true;
    }

    public static boolean isSuspiciousPlayer(Player player) {
        return suspiciousPlayers.contains(player.getUniqueId().toString());
    }

    public static void clearSuspiciousPlayer(Player player) {
        suspiciousPlayers.remove(player.getUniqueId().toString());
    }
}