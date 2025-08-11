package com.monkey.kt.utils.discord.security;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandLogger {
    private static final Map<String, AtomicInteger> commandStats = new ConcurrentHashMap<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static File logFile;

    public static void initialize(File pluginDataFolder) {
        if (!pluginDataFolder.exists()) {
            pluginDataFolder.mkdirs();
        }
        logFile = new File(pluginDataFolder, "command_usage.log");
    }

    public static void logCommandUsage(Player player, String command, String details) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = String.format("[%s] %s (%s) used %s - %s%n",
                timestamp, player.getName(), player.getUniqueId(), command, details);

        if (logFile != null) {
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(logEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String statKey = command + "_daily";
        commandStats.computeIfAbsent(statKey, k -> new AtomicInteger(0)).incrementAndGet();

        Bukkit.getLogger().info("[KT-COMMANDS] " + logEntry.trim());
    }

    public static int getCommandUsageToday(String command) {
        return commandStats.getOrDefault(command + "_daily", new AtomicInteger(0)).get();
    }

    public static void resetDailyStats() {
        commandStats.clear();
    }
}