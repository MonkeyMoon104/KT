package com.monkey.kt.effects.list.aurafarming.animation.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class AuraParticles {

    public static void spawnThickWhiteSparkle(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.END_ROD, loc, 10, 0.2, 0.2, 0.2, 0.01);
        world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 8, 0.2, 0.2, 0.2, 0.01);
        world.spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(255, 255, 255), 2.5f));
    }

    public static void spawnExplosionEffect(Location loc, int tick) {
        World world = loc.getWorld();
        if (world == null) return;

        double maxRadius = 10.0;
        double radius = Math.min(maxRadius, tick * 0.6);
        double height = radius / 2;

        world.spawnParticle(Particle.END_ROD, loc, 100, radius, height, radius, 0.04);

        world.spawnParticle(Particle.FIREWORKS_SPARK, loc.clone().add(0, height / 2, 0), 80, radius * 1.2, height / 2, radius * 1.2, 0.02);

        world.spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(200, 220, 255), 5.0f));

        for (int layer = 0; layer < 3; layer++) {
            double layerHeight = height * (layer - 1);
            int points = 12;
            for (int i = 0; i < points; i++) {
                double angle = (tick * 0.2) + (i * 2 * Math.PI / points);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                Location ringLoc = loc.clone().add(x, layerHeight, z);

                world.spawnParticle(Particle.REDSTONE, ringLoc, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(240, 250, 255), 3.5f));
            }
        }
    }

}
