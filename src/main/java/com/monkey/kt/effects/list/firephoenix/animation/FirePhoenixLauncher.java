package com.monkey.kt.effects.list.firephoenix.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.firephoenix.animation.util.FirePhoenixParticles;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Particle;
import org.bukkit.Color;

public class FirePhoenixLauncher {

    public static void launch(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60;
            final Color[] chargeColors = {
                    Color.fromRGB(255, 80, 0),
                    Color.fromRGB(255, 160, 0),
                    Color.fromRGB(255, 230, 100)
            };

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    FirePhoenixParticles.spawnPhoenixExplosion(plugin, center, killer);
                    world.playSound(center, Sound.ENTITY_BLAZE_DEATH, 2.0f, 0.8f);
                    cancel();
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
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
