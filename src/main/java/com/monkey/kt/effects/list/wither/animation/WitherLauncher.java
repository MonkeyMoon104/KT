package com.monkey.kt.effects.list.wither.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.wither.animation.util.WitherParticles;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WitherLauncher {

    public static void launch(KT plugin, Player killer, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        final int[] ticks = {0};
        final int maxTicks = 30;
        final double maxRadius = 5;

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;
                if (ticks[0] >= maxTicks) {
                    taskCompleted[0] = true;
                    WitherParticles.spawnWitherExplosion(loc);
                    world.playSound(loc, org.bukkit.Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);

                    new WitherOrbitalAnimation(plugin, loc.clone(), killer).start();

                    return;
                }

                double radius = (maxRadius * ticks[0]) / maxTicks;
                WitherParticles.spawnDarkSphere(world, loc, radius, 150);
                world.playSound(loc, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.4f);

                ticks[0]++;
            }
        }, loc, 0L, 2L);
    }
}