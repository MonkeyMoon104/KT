package com.monkey.kt.effects.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;

public class EffectUtils {

    public static void playRepeatingParticle(Plugin plugin, Location loc, Particle particle, int count,
                                             double offsetX, double offsetY, double offsetZ, double extra,
                                             long intervalTicks, int repetitions) {

        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int runCount = 0;

            @Override
            public void run() {
                if (runCount++ >= repetitions) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra);
            }
        }, 0L, intervalTicks);
    }
}
