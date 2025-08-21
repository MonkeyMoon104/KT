package com.monkey.kt.effects.list.stellarcollapse.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.stellarcollapse.animation.util.StellarParticles;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class StellarCollapseLauncher {

    public static void launch(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        final int[] ticks = {0};
        final int maxTicks = 40;
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask launcherTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;
                if (ticks[0] >= maxTicks) {
                    taskCompleted[0] = true;
                    StellarParticles.spawnCollapseImplosion(plugin, center, killer);
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 2.0f, 0.8f);
                    return;
                }

                double radius = 1 + (ticks[0] * 0.1);
                StellarParticles.spawnStellarSwirl(world, center, radius, 80);
                if (ticks[0] % 5 == 0) {
                    world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 2.0f, 1.5f - ticks[0] * 0.03f);
                }

                ticks[0]++;
            }
        }, center, 0L, 2L);
    }
}