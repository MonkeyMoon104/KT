package com.monkey.kt.effects.list.glowmissile.animation.util;

import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GlowMissileParticles {

    public static void spawnGlowSphere(World world, Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double phi = Math.acos(1 - 2.0 * (i + 0.5) / points);
            double theta = Math.PI * (1 + Math.sqrt(5)) * (i + 0.5);

            double x = radius * Math.cos(theta) * Math.sin(phi);
            double y = radius * Math.sin(theta) * Math.sin(phi);
            double z = radius * Math.cos(phi);

            Location particleLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.GLOW, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnGlowRing(World world, Location center, double radius, int points, double yOffset) {

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location particleLoc = center.clone().add(x, yOffset, z);
            world.spawnParticle(Particle.GLOW, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnTiltedGlowRing(World world, Location center, double radius, int points, double tiltDegrees) {
        double tiltRad = Math.toRadians(tiltDegrees);
        double sinTilt = Math.sin(tiltRad);
        double cosTilt = Math.cos(tiltRad);

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = z * sinTilt;
            double zTilted = z * cosTilt;
            Location particleLoc = center.clone().add(x, y, zTilted);
            world.spawnParticle(Particle.GLOW, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnEngineParticles(org.bukkit.World world, Location topBlock) {
        Location core = topBlock.clone();
        Location particleOrigin = core.clone().add(0, -0.5, 0);

        for (int yOffset = 0; yOffset < 10; yOffset++) {
            int count = 40;
            double spread = 0.1;

            Location layerLoc = particleOrigin.clone().add(0, -yOffset - 0.3, 0);

            world.spawnParticle(
                    Particle.GLOW,
                    layerLoc,
                    count,
                    spread, 0.1, spread,
                    0.005
            );
        }

        world.playSound(core, org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 0.7f, 1.2f);
    }
}
