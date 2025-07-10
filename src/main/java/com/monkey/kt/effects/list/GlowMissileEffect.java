package com.monkey.kt.effects.list;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GlowMissileEffect implements KillEffect {
    private final KT plugin;

    public GlowMissileEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (!WorldGuardUtils.isAvailable()) return;

        World world = loc.getWorld();
        if (world == null) return;

        Location startLoc = loc.getBlock().getLocation().add(0.5, 1, 0.5);

        spawnGlowMissile(startLoc);
    }

    private void spawnGlowMissile(Location startLoc) {
        World world = startLoc.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int height = 0;
            final int maxHeight = 15;
            List<Location> missileBlocks = new ArrayList<>();

            @Override
            public void run() {
                if (height >= maxHeight) {
                    clearBlocks(missileBlocks);
                    Location explosionCenter = startLoc.clone().add(0, maxHeight + 1, 0);
                    runExplosionSequence(world, explosionCenter);
                    world.playSound(explosionCenter, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 1.0f);
                    cancel();
                    return;
                }

                clearBlocks(missileBlocks);
                missileBlocks.clear();

                Location base = startLoc.clone().add(0, height, 0);
                Location top = base.clone().add(0, 1, 0);

                placeMissileBlocks(base, top, missileBlocks);
                spawnEngineParticles(world, top);

                height++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void runExplosionSequence(World world, Location center) {
        new BukkitRunnable() {
            double sphereRadius = 6.0;
            final double minRadius = 0.1;
            final double shrinkStep = 0.7;

            int ringPoints = 100;
            double ring1Radius = 0;
            double ring2Radius = 0;
            final double maxRingRadius = 15;
            final double ringExpandStep = 0.5;

            enum Phase { SHRINK_SPHERE, EXPAND_RINGS, DONE }
            Phase phase = Phase.SHRINK_SPHERE;

            boolean sphereStarted = false;

            @Override
            public void run() {
                if (phase == Phase.SHRINK_SPHERE) {
                    if (!sphereStarted) {
                        world.playSound(center, Sound.ENTITY_WARDEN_SONIC_CHARGE, 2.0f, 1.0f);
                        sphereStarted = true;
                    }

                    if (sphereRadius <= minRadius) {
                        phase = Phase.EXPAND_RINGS;
                        world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
                        return;
                    }

                    spawnGlowSphere(world, center, sphereRadius, 300);
                    sphereRadius -= shrinkStep;

                } else if (phase == Phase.EXPAND_RINGS) {
                    if (ring1Radius >= maxRingRadius) {
                        cancel();
                        return;
                    }

                    spawnGlowRing(world, center, ring1Radius, ringPoints, 0);
                    spawnTiltedGlowRing(world, center, ring2Radius, ringPoints, 20);
                    ring1Radius += ringExpandStep;
                    ring2Radius += ringExpandStep;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void spawnGlowSphere(World world, Location center, double radius, int points) {
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

    private void spawnGlowRing(World world, Location center, double radius, int points, double yOffset) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location particleLoc = center.clone().add(x, yOffset, z);
            world.spawnParticle(Particle.GLOW, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private void spawnTiltedGlowRing(World world, Location center, double radius, int points, double tiltDegrees) {
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

    private void clearBlocks(List<Location> blocks) {
        for (Location loc : blocks) {
            loc.getBlock().setType(Material.AIR);
        }
    }

    private void placeMissileBlocks(Location base, Location top, List<Location> missileBlocks) {
        base.getBlock().setType(Material.IRON_BLOCK);
        top.getBlock().setType(Material.IRON_BLOCK);
        missileBlocks.add(base);
        missileBlocks.add(top);

        for (Vector offset : List.of(
                new Vector(1, 0, 0),
                new Vector(-1, 0, 0),
                new Vector(0, 0, 1),
                new Vector(0, 0, -1)
        )) {
            Location fenceLoc = base.clone().add(offset);
            fenceLoc.getBlock().setType(Material.NETHER_BRICK_FENCE);
            missileBlocks.add(fenceLoc);
        }
    }

    private void spawnEngineParticles(World world, Location topBlock) {
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

        world.playSound(core, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 0.7f, 1.2f);
    }
}