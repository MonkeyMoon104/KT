package com.monkey.kt.effects.list.cryocore.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CryoCoreParticles {

    public static void spawnIceExplosion(World world, Location center, int tick, int spiker, Plugin plugin, Player killer) {

        DamageConfig damageConfig = DamageUtils.getDamageConfig("cryocore", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
        }

        int points = Math.min(15, 10 + tick / 4);
        double progress = Math.min(tick / 30.0, 1.0);

        for (int i = 0; i < points; i++) {
            double radius = Math.random() * (double) spiker * progress;

            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.cos(phi) + 1.0;
            double z = radius * Math.sin(phi) * Math.sin(theta);

            Location pLoc = center.clone().add(x, y, z);

            if (tick % 3 == 0) {
                world.spawnParticle(Particle.SNOWFLAKE, pLoc, 2, 0.1, 0.1, 0.1, 0.02);
                world.spawnParticle(Particle.DUST, pLoc, 1, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(180, 255, 255), 1.4f));
            }

            if (tick % 4 == 0) {
                world.spawnParticle(Particle.BLOCK, pLoc, 1, 0.05, 0.05, 0.05, 0.15, Material.ICE.createBlockData());
                world.spawnParticle(Particle.CLOUD, pLoc, 1, 0.15, 0.15, 0.15, 0.02);
            }

            if (i % 4 == 0) {
                world.spawnParticle(Particle.ITEM_SNOWBALL, pLoc, 1, 0.1, 0.1, 0.1, 0.01);
            }
        }
    }

    public static void spawnInclinedBurst(KT plugin, Location center, int spiker) {
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 10;
            final double maxRadius = spiker;
            final int points = 16;

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

                    world.spawnParticle(Particle.ENCHANTED_HIT, pLoc, 1, 0.05, 0.05, 0.05, 0.02);
                    world.spawnParticle(Particle.DUST, pLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.AQUA, 1.0f));

                    if (i % 3 == 0) {
                        world.spawnParticle(Particle.FIREWORK, pLoc, 1, 0.07, 0.07, 0.07, 0.03);
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    public static void spawnFinalCrystalFade(Plugin plugin, World world, Location center, int spiker) {
        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 26;
            final double maxRadius = spiker;

            @Override
            public void run() {
                if (tick > maxTicks) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 10; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = Math.random() * maxRadius;

                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    if (i < 6) {
                        double riseY = 0.5 + tick * 0.1 + Math.random() * 0.5;
                        Location rising = center.clone().add(x, riseY, z);

                        world.spawnParticle(Particle.DUST, rising, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(200, 255, 255), 1.1f));

                        if (i % 3 == 0) {
                            world.spawnParticle(Particle.FIREWORK, rising, 1, 0.03, 0.03, 0.03, 0.02);
                        }
                    }

                    if (i >= 5) {
                        double fallY = 4.5 - tick * 0.08 - Math.random() * 0.5;
                        Location falling = center.clone().add(x, fallY, z);

                        world.spawnParticle(Particle.SNOWFLAKE, falling, 1, 0.05, 0.05, 0.05, 0.02);

                        if (i % 3 == 0) {
                            world.spawnParticle(Particle.CLOUD, falling, 1, 0.05, 0.05, 0.05, 0.01);
                        }
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }
}