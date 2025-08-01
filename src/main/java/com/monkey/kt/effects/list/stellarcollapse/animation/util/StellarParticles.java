package com.monkey.kt.effects.list.stellarcollapse.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StellarParticles {

    public static void spawnStellarSwirl(World world, Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.sin(angle * 3) * 0.5;

            Location particleLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.REDSTONE, particleLoc, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1.5f));
            world.spawnParticle(Particle.DRAGON_BREATH, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);
        }
    }

    public static void spawnCollapseImplosion(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        DamageConfig damageConfig = DamageUtils.getDamageConfig("stellarcollapse", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
        }

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step > 24) {
                    for (int y = 0; y < 50; y++) {
                        Location loc = center.clone().add(0, y * 0.3, 0);
                        world.spawnParticle(Particle.END_ROD, loc, 4, 0.08, 0.08, 0.08, 0.01);
                        world.spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 5, 0.1, 0.1, 0.1, 0.0);
                    }

                    world.spawnParticle(Particle.FLASH, center, 1);
                    world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.2f);
                    cancel();
                    return;
                }

                double radius = 2.8 - step * 0.1;
                for (int i = 0; i < 70; i++) {
                    double angle = 2 * Math.PI * i / 70 + step * 0.3;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = (Math.sin(step * 0.4 + i * 0.15)) * 0.4;

                    Location pLoc = center.clone().add(x, y, z);
                    world.spawnParticle(Particle.DRAGON_BREATH, pLoc, 1, 0.02, 0.02, 0.02, 0.01);
                    world.spawnParticle(Particle.REDSTONE, pLoc, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 100), 1.2f));
                }

                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
