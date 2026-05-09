package com.monkey.kt.effects.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;

public class EffectUtils {

    public static void playRepeatingParticle(KT plugin, String effectId, Location loc, Particle particle, int count,
                                             double offsetX, double offsetY, double offsetZ, double extra,
                                             long intervalTicks, int repetitions) {
        int scaledCount = plugin.getParticlePerformanceManager().scaleParticleCount(effectId, count, false);
        long scaledInterval = plugin.getParticlePerformanceManager().scaleTickInterval(effectId, intervalTicks, false);
        int scaledRepetitions = plugin.getParticlePerformanceManager().scaleLoopCount(effectId, repetitions, false);

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
                        loc.getWorld().spawnParticle(particle, loc, scaledCount, offsetX, offsetY, offsetZ, extra);
                    }
                } catch (Exception e) {
                    taskCompleted[0] = true;
                }
            }
        }, loc, 0L, scaledInterval);
    }
}
