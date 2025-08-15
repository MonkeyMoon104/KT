package com.monkey.kt.effects.list.enchantcolumn.animation.util;

import org.bukkit.*;
import org.bukkit.Particle.DustOptions;

public class ParticleUtils {

    public static void spawnTipCircle(World world, Location tip, double radius, Color color) {
        int points = 30;
        DustOptions dust = new DustOptions(color, 1.5f);
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = tip.clone().add(x, 0, z);
            world.spawnParticle(Particle.DUST, loc, 0, 0, 0, 0, 0, dust);
        }
    }
}
