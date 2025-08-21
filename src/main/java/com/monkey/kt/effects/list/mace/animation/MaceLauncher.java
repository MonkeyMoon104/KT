package com.monkey.kt.effects.list.mace.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.mace.animation.util.MaceParticles;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
public class MaceLauncher {

    public static void launch(KT plugin, Player killer, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimer(plugin, new Runnable() {
            int ticks = 0;
            final int maxTicks = 25;
            final double maxRadius = 4;

            @Override
            public void run() {
                if (taskCompleted[0]) return;

                ticks++;

                if (ticks >= maxTicks) {
                    taskCompleted[0] = true;
                    MaceParticles.spawnGoldenShockwave(loc);
                    world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
                    new MaceOrbitalAnimation(plugin, loc.clone(), killer).start();
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = (maxRadius * ticks) / maxTicks;
                MaceParticles.spawnChargingSphere(world, loc, radius, 120);
                world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.5f);

            }
        }, 0L, 1L);
    }
}
