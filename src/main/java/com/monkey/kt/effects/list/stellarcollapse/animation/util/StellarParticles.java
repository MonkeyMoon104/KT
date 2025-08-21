package com.monkey.kt.effects.list.stellarcollapse.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class StellarParticles {

    public static void spawnStellarSwirl(World world, Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.sin(angle * 3) * 0.5;

            Location particleLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.DUST, particleLoc, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1.5f));
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

        final int[] step = {0};
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask implosionTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;
                if (step[0] > 24) {
                    taskCompleted[0] = true;

                    for (int y = 0; y < 50; y++) {
                        Location loc = center.clone().add(0, y * 0.3, 0);
                        world.spawnParticle(Particle.END_ROD, loc, 4, 0.08, 0.08, 0.08, 0.01);
                        world.spawnParticle(Particle.ENCHANT, loc, 5, 0.1, 0.1, 0.1, 0.0);
                    }

                    world.spawnParticle(Particle.FLASH, center, 1);
                    world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.2f);
                    return;
                }

                double radius = 2.8 - step[0] * 0.1;
                for (int i = 0; i < 70; i++) {
                    double angle = 2 * Math.PI * i / 70 + step[0] * 0.3;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = (Math.sin(step[0] * 0.4 + i * 0.15)) * 0.4;

                    Location pLoc = center.clone().add(x, y, z);
                    world.spawnParticle(Particle.DRAGON_BREATH, pLoc, 1, 0.02, 0.02, 0.02, 0.01);
                    world.spawnParticle(Particle.DUST, pLoc, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 100), 1.2f));
                }

                step[0]++;
            }
        }, center, 0L, 1L);
    }
}