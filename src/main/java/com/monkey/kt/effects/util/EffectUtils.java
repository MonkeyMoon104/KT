package com.monkey.kt.effects.util;

import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;

public class EffectUtils {

    public static void playRepeatingParticle(Plugin plugin, Location loc, Particle particle, int count,
                                             double offsetX, double offsetY, double offsetZ, double extra,
                                             long intervalTicks, int repetitions) {

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            int runCount = 0;

            @Override
            public void run() {
                if (taskCompleted[0]) return;

                if (runCount++ >= repetitions) {
                    taskCompleted[0] = true;
                    return;
                }

                try {
                    if (loc.getWorld() != null) {
                        loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra);
                    }
                } catch (Exception e) {
                    taskCompleted[0] = true;
                }
            }
        }, loc, 0L, intervalTicks);
    }
}