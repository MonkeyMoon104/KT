package com.monkey.kt.effects.list.cryocore.animation.util;

import com.monkey.kt.KT;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CryoCoreParticles {

    public static void spawnIceExplosion(World world, Location center, int tick, int spiker) {
        int points = 60;
        double progress = Math.min(tick / 30.0, 1.0);

        for (int i = 0; i < points; i++) {
            double radius = Math.random() * (double) spiker * progress;

            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.cos(phi) + 1.0;
            double z = radius * Math.sin(phi) * Math.sin(theta);

            Location pLoc = center.clone().add(x, y, z);

            world.spawnParticle(Particle.SNOWFLAKE, pLoc, 4, 0.1, 0.1, 0.1, 0.02);
            world.spawnParticle(Particle.REDSTONE, pLoc, 1, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(180, 255, 255), 1.4f));
            world.spawnParticle(Particle.BLOCK_CRACK, pLoc, 1, 0.05, 0.05, 0.05, 0.15, Material.ICE.createBlockData());
            world.spawnParticle(Particle.CLOUD, pLoc, 3, 0.15, 0.15, 0.15, 0.02);
            world.spawnParticle(Particle.SNOWBALL, pLoc, 2, 0.1, 0.1, 0.1, 0.01);
            world.spawnParticle(Particle.ASH, pLoc, 3, 0.07, 0.07, 0.07, 0.02);
        }
    }

    public static void spawnInclinedBurst(KT plugin, Location center, int spiker) {
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 12;
            final double maxRadius = spiker;
            final int points = 40;

            @Override
            public void run() {
                if (tick > maxTicks) {
                    cancel();
                    return;
                }

                double progress = (double) tick / maxTicks;
                double radius = maxRadius * progress;

                for (int i = 0; i < points; i++) {
                    double theta = Math.random() * 2 * Math.PI;
                    double phi = Math.acos(2 * Math.random() - 1);

                    double x = radius * Math.sin(phi) * Math.cos(theta);
                    double y = 1.0 + tick * 0.12 + radius * Math.cos(phi);
                    double z = radius * Math.sin(phi) * Math.sin(theta);

                    Location pLoc = center.clone().add(x, y, z);

                    world.spawnParticle(Particle.CRIT_MAGIC, pLoc, 3, 0.05, 0.05, 0.05, 0.02);
                    world.spawnParticle(Particle.REDSTONE, pLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.AQUA, 1.2f));
                    world.spawnParticle(Particle.FIREWORKS_SPARK, pLoc, 3, 0.07, 0.07, 0.07, 0.03);
                    world.spawnParticle(Particle.SLIME, pLoc, 3, 0.05, 0.05, 0.05, 0.01);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public static void spawnFinalCrystalFade(Plugin plugin, World world, Location center, int spiker) {
        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 30;
            final double maxRadius = spiker;

            @Override
            public void run() {
                if (tick > maxTicks) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 25; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = Math.random() * maxRadius;

                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    double riseY = 0.5 + tick * 0.1 + Math.random() * 0.5;
                    Location rising = center.clone().add(x, riseY, z);

                    world.spawnParticle(Particle.REDSTONE, rising, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(200, 255, 255), 1.3f));
                    world.spawnParticle(Particle.FIREWORKS_SPARK, rising, 3, 0.03, 0.03, 0.03, 0.02);

                    double fallY = 4.5 - tick * 0.08 - Math.random() * 0.5;
                    Location falling = center.clone().add(x, fallY, z);

                    world.spawnParticle(Particle.SNOWFLAKE, falling, 2, 0.05, 0.05, 0.05, 0.02);
                    world.spawnParticle(Particle.CLOUD, falling, 3, 0.05, 0.05, 0.05, 0.01);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
