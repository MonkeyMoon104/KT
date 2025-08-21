package com.monkey.kt.effects.list.mace.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.mace.animation.util.MaceParticles;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MaceOrbitalAnimation {

    private final KT plugin;
    private final Location center;
    private final Player killer;

    public MaceOrbitalAnimation(KT plugin, Location center, Player killer) {
        this.plugin = plugin;
        this.center = center;
        this.killer = killer;
    }

    public void start() {
        World world = center.getWorld();
        if (world == null) return;
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimer(plugin, new Runnable() {
            double angle = 0;
            int ticks = 0;

            @Override
            public void run() {
                if (taskCompleted[0]) return;

                ticks++;
                if (ticks > 60) {
                    taskCompleted[0] = true;
                    MaceParticles.spawnFinalBurst(center);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.2f);
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                angle += Math.PI / 20;
                double radius = 2.5 + Math.sin(ticks / 5.0) * 0.5;

                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                Location orbLoc = center.clone().add(x, Math.sin(angle * 2) * 1.2 + 1, z);

                world.spawnParticle(Particle.END_ROD, orbLoc, 1, 0, 0, 0, 0.01);
                world.spawnParticle(Particle.ENCHANTED_HIT, orbLoc, 1, 0.05, 0.05, 0.05, 0.01);
                if (ticks % 5 == 0) {
                    world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.8f);
                }
            }
        }, 0L, 1L);
    }
}
