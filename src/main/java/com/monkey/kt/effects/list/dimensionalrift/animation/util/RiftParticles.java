package com.monkey.kt.effects.list.dimensionalrift.animation.util;

import org.bukkit.*;
import org.bukkit.util.Vector;

import java.util.Random;

public class RiftParticles {

    private static final Random random = new Random();

    public static void spawnHugeOpeningRing(World world, Location center, int tick) {
        double radius = 3 + tick * 0.08;
        int points = 140;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points + tick * 0.2;
            double x = Math.cos(angle) * radius + (random.nextDouble() - 0.5) * 0.6;
            double y = 1 + Math.sin(angle * 6 + tick * 0.7) * 0.7;
            double z = Math.sin(angle) * radius + (random.nextDouble() - 0.5) * 0.6;

            Location loc = center.clone().add(x, y, z);

            world.spawnParticle(Particle.PORTAL, loc, 1, 0.07, 0.07, 0.07, 0.03);
            if (tick % 3 == 0) {
                world.spawnParticle(Particle.BLOCK, loc, 2, 0.1, 0.1, 0.1, 0.02, Material.OBSIDIAN.createBlockData());
            }
            if (tick % 6 == 0 && i % 7 == 0) {
                world.spawnParticle(Particle.FIREWORK, loc, 5, 0.05, 0.05, 0.05, 0.03);
            }
        }
    }

    public static void spawnGlitchVortex(World world, Location center, int tick) {
        double baseRadius = 1.5 + 0.6 * Math.sin(tick * 0.45);
        int points = 80;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points + tick * 0.5;
            double x = Math.cos(angle) * baseRadius * (0.9 + 0.2 * Math.sin(tick * 0.7 + i));
            double y = 1 + Math.sin(tick * 0.7 + i * 0.8) * 0.5 + Math.cos(tick * 0.3 + i) * 0.3;
            double z = Math.sin(angle) * baseRadius * (0.9 + 0.2 * Math.cos(tick * 0.4 + i));

            Location loc = center.clone().add(x, y, z);

            world.spawnParticle(Particle.DRAGON_BREATH, loc, 2, 0.03, 0.03, 0.03, 0.015);
            if (tick % 5 == 0 && i % 15 == 0) {
                world.spawnParticle(Particle.PORTAL, loc, 1, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    public static void spawnGlitchRingMovement(World world, Location center, int tick) {
        double radius = 5.5 + 0.2 * Math.sin(tick * 0.7);
        int points = 100;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points + tick * 0.45;
            double offsetX = (random.nextDouble() - 0.5) * 0.6;
            double offsetY = (random.nextDouble() - 0.5) * 0.6;
            double offsetZ = (random.nextDouble() - 0.5) * 0.6;

            double x = Math.cos(angle) * radius + offsetX;
            double y = 1 + offsetY;
            double z = Math.sin(angle) * radius + offsetZ;

            Location loc = center.clone().add(x, y, z);

            world.spawnParticle(Particle.FIREWORK, loc, 2, 0.015, 0.015, 0.015, 0.015);
            if (tick % 3 == 0) {
                world.spawnParticle(Particle.PORTAL, loc, 2, 0.07, 0.07, 0.07, 0.025);
            }
            if (tick % 4 == 0 && i % 10 == 0) {
                world.spawnParticle(Particle.FLASH, loc, 1);
            }
        }
    }

    public static void spawnEnergyBeams(World world, Location center, int tick) {
        int beamCount = 20;
        double maxHeight = 12.0;
        for (int i = 0; i < beamCount; i++) {
            double angle = 2 * Math.PI * i / beamCount + tick * 0.6;
            double radius = 5.0 + 0.2 * Math.sin(tick * 0.5);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            for (double y = 0; y < maxHeight; y += 0.3) {
                Location loc = center.clone().add(x, y, z);
                world.spawnParticle(Particle.END_ROD, loc, 3, 0.015, 0.015, 0.015, 0.015);
                if (y > maxHeight - 3) {
                    world.spawnParticle(Particle.FLASH, loc, 2);
                }
                if (y % 1 < 0.3 && (tick + i) % 5 == 0) {
                    Location jitter = loc.clone().add(
                            (random.nextDouble() - 0.5) * 0.2,
                            (random.nextDouble() - 0.5) * 0.2,
                            (random.nextDouble() - 0.5) * 0.2
                    );
                    world.spawnParticle(Particle.PORTAL, jitter, 1, 0.04, 0.04, 0.04, 0.02);
                }
            }
        }
    }

    public static void spawnClosingSuction(World world, Location center, int tick) {
        int points = 120;
        double startRadius = 6.0;
        double progress = tick / 30.0;
        double radius = startRadius * (1 - progress);

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points + tick * 0.7;
            double x = Math.cos(angle) * radius;
            double y = 1 + Math.sin(tick * 0.5 + i * 0.8) * 0.6;
            double z = Math.sin(angle) * radius;

            Location loc = center.clone().add(x, y, z);

            Vector direction = center.toVector().subtract(loc.toVector()).normalize().multiply(0.2 + progress * 0.5);
            loc.add(direction);

            world.spawnParticle(Particle.PORTAL, loc, 3, 0.03, 0.03, 0.03, 0.015);
            if (tick % 3 == 0) {
                world.spawnParticle(Particle.BLOCK, loc, 2, 0.1, 0.1, 0.1, 0.03, Material.OBSIDIAN.createBlockData());
            }
            if (tick % 5 == 0 && i % 12 == 0) {
                world.spawnParticle(Particle.FIREWORK, loc, 4, 0.05, 0.05, 0.05, 0.03);
            }
        }
    }
}
