package com.monkey.kt.effects.list.mace.animation.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class MaceParticles {

    public static void spawnChargingSphere(World world, Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double phi = Math.acos(1 - 2.0 * (i + 0.5) / points);
            double theta = Math.PI * (1 + Math.sqrt(5)) * (i + 0.5);

            double x = radius * Math.cos(theta) * Math.sin(phi);
            double y = radius * Math.sin(theta) * Math.sin(phi);
            double z = radius * Math.cos(phi);

            Location particleLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.DUST, particleLoc, 0,
                    new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));
        }
    }

    public static void spawnGoldenShockwave(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        for (int i = 0; i < 360; i += 8) {
            double radians = Math.toRadians(i);
            double x = Math.cos(radians) * 3;
            double z = Math.sin(radians) * 3;
            Location ringLoc = center.clone().add(x, 0.2, z);

            world.spawnParticle(Particle.FIREWORK, ringLoc, 3, 0.1, 0.1, 0.1, 0.02);
            world.spawnParticle(Particle.DUST, ringLoc, 1,
                    new Particle.DustOptions(Color.fromRGB(255, 223, 50), 1.5f));
        }
    }

    public static void spawnFinalBurst(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.EXPLOSION, center, 1);
        world.spawnParticle(Particle.FLASH, center, 1);
        for (int i = 0; i < 80; i++) {
            double angle = Math.random() * Math.PI * 2;
            double y = (Math.random() - 0.5) * 2;
            double radius = Math.sqrt(Math.random()) * 4;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location pLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.END_ROD, pLoc, 1, 0, 0, 0, 0.01);
        }
    }
}
