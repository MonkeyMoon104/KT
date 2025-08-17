package com.monkey.kt.events.meteorshower;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.events.helper.GameEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class MeteorShowerEvent implements GameEvent {

    private final KT plugin;
    private BukkitTask dropTask;
    private BukkitTask endTask;
    private boolean eventEnded = false;
    private long endTimestamp = 0L;

    public MeteorShowerEvent(KT plugin) {
        this.plugin = plugin;
    }

    public void start(Player killer, Player victim) {
        EconomyManager eco = plugin.getEconomyManager();
        if (!plugin.getConfig().getBoolean("events.list.meteor_shower.enabled", true)) return;

        int meteorCount = plugin.getConfig().getInt("events.list.meteor_shower.settings.meteor_count", 15);
        String materialName = plugin.getConfig().getString("events.list.meteor_shower.settings.material", "DIAMOND");
        int duration = plugin.getConfig().getInt("events.list.meteor_shower.settings.duration", 20);

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
                .replace("%event%", "Meteor Shower")
                .replace("%end%", formatDuration(duration));
        Bukkit.broadcastMessage(msg.replace("&", "§"));

        this.endTimestamp = System.currentTimeMillis() + (duration * 1000L);

        MeteorShowerDropTask dropTask = new MeteorShowerDropTask(plugin, eco, world, killer, material, meteorCount);
        this.dropTask = dropTask.runTaskTimer(plugin, 0L, 5L);

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
        MeteorShowerListener.removeAllItems();

        String finishMsgKey = timeExpired ?
                "events.broadcast.finish_not_collected" :
                "events.broadcast.finish_collected";

        String finishMsg = plugin.getConfig().getString(finishMsgKey, "");
        if (finishMsg != null && !finishMsg.isEmpty()) {
            plugin.getServer().broadcastMessage(finishMsg.replace("&", "§"));
        }

        if (plugin.getEventManager() != null) {
            plugin.getEventManager().clearMeteorEvent();
        }
    }

    public void stop() {
        if (dropTask != null) dropTask.cancel();
        if (endTask != null) endTask.cancel();
    }

    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0) return String.format("%dm %ds", minutes, seconds);
        return String.format("%ds", seconds);
    }

    public String getRemainingTimeFormatted() {
        return formatDuration((int) getRemainingTimeSeconds());
    }

    @Override
    public String getEventName() {
        return "Meteor Shower";
    }
}
