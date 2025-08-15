package com.monkey.kt.effects.list.wither.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WitherParticles {

    public static void spawnDarkSphere(World world, Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double phi = Math.acos(1 - 2.0 * (i + 0.5) / points);
            double theta = Math.PI * (1 + Math.sqrt(5)) * (i + 0.5);

            double x = radius * Math.cos(theta) * Math.sin(phi);
            double y = radius * Math.sin(theta) * Math.sin(phi);
            double z = radius * Math.cos(phi);

            Location particleLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.LARGE_SMOKE, particleLoc, 0, 0, 0, 0, 0);
        }
    }

    public static void spawnWitherExplosion(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.FLASH, center, 1);
        world.spawnParticle(Particle.EXPLOSION, center, 1);

        for (int i = 0; i < 360; i += 10) {
            double radians = Math.toRadians(i);
            double x = Math.cos(radians) * 4;
            double z = Math.sin(radians) * 4;

            Location ringLoc = center.clone().add(x, 0.5, z);
            world.spawnParticle(Particle.WITCH, ringLoc, 5, 0.2, 0.2, 0.2, 0.01);
            world.spawnParticle(Particle.PORTAL, ringLoc, 10, 0.2, 0.2, 0.2, 0.01);
        }
    }

    public static void launchBurstSkulls(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        DamageConfig damageConfig = DamageUtils.getDamageConfig("wither", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
        }

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            Vector dir = new Vector(Math.cos(angle), -0.5, Math.sin(angle)).normalize();

            Location launchFrom = center.clone().add(0, 1.2, 0);
            WitherSkull skull = world.spawn(launchFrom, WitherSkull.class);
            skull.setDirection(dir);
            skull.setVelocity(dir.multiply(0.6));
            skull.setInvulnerable(true);
            skull.setYield(0);
            skull.setCharged(true);
            skull.setGravity(true);
            skull.setSilent(true);
            skull.setMetadata("kt_wither_skull", new FixedMetadataValue(plugin, true));

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (!skull.isValid() || skull.isDead()) {
                        cancel();
                        return;
                    }
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, skull.getLocation(), 2, 0, 0, 0, 0.01);
                    world.spawnParticle(Particle.LARGE_SMOKE, skull.getLocation(), 1, 0.05, 0.05, 0.05, 0.01);
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    public static void spawnFinalImplosion(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (step > 30) {
                    world.spawnParticle(Particle.EXPLOSION, center, 3);
                    world.spawnParticle(Particle.FIREWORK, center, 20, 0.5, 0.5, 0.5, 0.1);
                    world.spawnParticle(Particle.END_ROD, center, 40, 0.7, 0.7, 0.7, 0.01);
                    world.playSound(center, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.5f);
                    launchBurstSkulls(plugin, center, killer);
                    cancel();
                    return;
                }

                double radius = 5 - (step * 0.15);
                for (int i = 0; i < 60; i++) {
                    double angle = 2 * Math.PI * i / 60 + step * 0.2;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = Math.sin(step * 0.3 + i * 0.1) * 1.5;

                    Location pLoc = center.clone().add(x, 1.5 + y, z);
                    world.spawnParticle(Particle.PORTAL, pLoc, 2, 0.05, 0.05, 0.05, 0.01);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, pLoc, 1, 0, 0, 0, 0.01);
                    world.spawnParticle(Particle.DUST, pLoc, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(90, 0, 140), 1.2f));
                }

                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
