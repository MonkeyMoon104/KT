package com.monkey.kt.effects.list.firephoenix.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.firephoenix.animation.util.FirePhoenixParticles;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.Color;

public class FirePhoenixLauncher {

    public static void launch(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimer(plugin, new Runnable() {
            int ticks = 0;
            final int maxTicks = 60;
            final Color[] chargeColors = {
                    Color.fromRGB(255, 80, 0),
                    Color.fromRGB(255, 160, 0),
                    Color.fromRGB(255, 230, 100)
            };

            @Override
            public void run() {
                if (taskCompleted[0]) return;

                if (ticks >= maxTicks) {
                    taskCompleted[0] = true;
                    FirePhoenixParticles.spawnPhoenixExplosion(plugin, center, killer);
                    world.playSound(center, Sound.ENTITY_BLAZE_DEATH, 2.0f, 0.8f);
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = 0.3 + (ticks * 0.05);
                int particles = 15 + ticks * 2;
                Color color = chargeColors[ticks % chargeColors.length];

                for (int i = 0; i < particles; i++) {
                    double theta = Math.random() * 2 * Math.PI;
                    double phi = Math.acos(2 * Math.random() - 1);
                    double x = radius * Math.sin(phi) * Math.cos(theta);
                    double y = radius * Math.cos(phi);
                    double z = radius * Math.sin(phi) * Math.sin(theta);

                    world.spawnParticle(Particle.DUST,
                            center.clone().add(x, y, z),
                            0, 0, 0, 0,
                            new Particle.DustOptions(color, 1.5f));
                    world.spawnParticle(Particle.FLAME,
                            center.clone().add(x * 0.8, y * 0.8, z * 0.8),
                            0, 0, 0, 0, 0.01);
                }

                if (ticks % 5 == 0) {
                    world.playSound(center, Sound.ITEM_FIRECHARGE_USE, 1.5f, 1.2f - ticks * 0.02f);
                }

                ticks++;
            }
        }, 0L, 2L);
    }
}
