package com.monkey.kt.effects.list.glowmissile.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.glowmissile.animation.util.GlowMissileParticles;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class GlowMissileExplosion {
    private enum Phase { SHRINK_SPHERE, EXPAND_RINGS, DONE }

    public static void start(KT plugin, Location center) {
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            double sphereRadius = 6.0;
            final double minRadius = 0.1;
            final double shrinkStep = 0.7;

            int ringPoints = 100;
            double ring1Radius = 0;
            double ring2Radius = 0;
            final double maxRingRadius = 15;
            final double ringExpandStep = 0.5;
            Phase phase = Phase.SHRINK_SPHERE;

            boolean sphereStarted = false;

            @Override
            public void run() {
                if (phase == Phase.SHRINK_SPHERE) {
                    if (!sphereStarted) {
                        world.playSound(center, org.bukkit.Sound.ENTITY_WARDEN_SONIC_CHARGE, 2.0f, 1.0f);
                        sphereStarted = true;
                    }

                    if (sphereRadius <= minRadius) {
                        phase = Phase.EXPAND_RINGS;
                        world.playSound(center, org.bukkit.Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
                        return;
                    }

                    GlowMissileParticles.spawnGlowSphere(world, center, sphereRadius, 300);
                    sphereRadius -= shrinkStep;

                } else if (phase == Phase.EXPAND_RINGS) {
                    if (ring1Radius >= maxRingRadius) {
                        cancel();
                        return;
                    }

                    GlowMissileParticles.spawnGlowRing(world, center, ring1Radius, ringPoints, 0);
                    GlowMissileParticles.spawnTiltedGlowRing(world, center, ring2Radius, ringPoints, 20);
                    ring1Radius += ringExpandStep;
                    ring2Radius += ringExpandStep;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
