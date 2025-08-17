package com.monkey.kt.events.moneyfountain;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.events.helper.GameEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class MoneyFountainEvent implements GameEvent {

    private final KT plugin;
    private BukkitTask dropTask;
    private BukkitTask endTask;
    private boolean eventEnded = false;
    private long endTimestamp = 0L;

    public MoneyFountainEvent(KT plugin) {
        this.plugin = plugin;
    }

    public void start(Player killer, Player victim) {
        EconomyManager eco = plugin.getEconomyManager();
        if (!plugin.getConfig().getBoolean("events.list.money_fountain.enabled", true)) return;

        int dropAmount = plugin.getConfig().getInt("events.list.money_fountain.settings.drop", 64);
        String materialName = plugin.getConfig().getString("events.list.money_fountain.settings.material", "EMERALD");
        int duration = plugin.getConfig().getInt("events.list.money_fountain.settings.duration", 60);

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Invalid material in config: " + materialName);
            return;
        }

        Location loc = killer.getLocation();
        World world = loc.getWorld();

        String raw = plugin.getConfig().getString("events.broadcast.players", "");
        String msg = raw
                .replace("%killer%", killer.getName())
                .replace("%deaded%", victim.getName())
                .replace("%event%", "Money Fountain")
                .replace("%end%", formatDuration(duration));
        Bukkit.broadcastMessage(msg.replace("&", "§"));

        this.endTimestamp = System.currentTimeMillis() + (duration * 1000L);

        MoneyFountainDropTask dropTask = new MoneyFountainDropTask(plugin, eco, world, loc, material, dropAmount);
        this.dropTask = dropTask.runTaskTimer(plugin, 0L, 2L);

        this.endTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!eventEnded) {
                endEvent(true);
            }
        }, duration * 20L);
    }

    public long getRemainingTimeSeconds() {
        if (eventEnded) return 0;
        long remainingMillis = endTimestamp - System.currentTimeMillis();
        return Math.max(0, remainingMillis / 1000);
    }

    public void endEvent(boolean timeExpired) {
        if (eventEnded) return;
        eventEnded = true;

        stop();
        MoneyFountainListener.removeAllItems();

        if (timeExpired) {
            String finishnotMsg = plugin.getConfig().getString("events.broadcast.finish_not_collected", "");
            if (finishnotMsg != null && !finishnotMsg.isEmpty()) {
                plugin.getServer().broadcastMessage(finishnotMsg.replace("&", "§"));
            }
        } else {
            String finishMsg = plugin.getConfig().getString("events.broadcast.finish_collected", "");
            if (finishMsg != null && !finishMsg.isEmpty()) {
                plugin.getServer().broadcastMessage(finishMsg.replace("&", "§"));
            }
        }

        if (plugin.getEventManager() != null) {
            plugin.getEventManager().clearFountainEvent();
        }
    }

    public void stop() {
        if (dropTask != null) {
            dropTask.cancel();
            dropTask = null;
        }
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
    }

    private String formatDuration(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    public String getRemainingTimeFormatted() {
        long remainingSeconds = getRemainingTimeSeconds();

        long days = remainingSeconds / 86400;
        long hours = (remainingSeconds % 86400) / 3600;
        long minutes = (remainingSeconds % 3600) / 60;
        long seconds = remainingSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    @Override
    public String getEventName() {
        return "Money Fountain";
    }
}
