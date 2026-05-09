package com.monkey.kt.effects.list.prismaticnova.animation;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Player;

public final class PrismaticNovaAnimation {
    private static final String EFFECT_ID = "prismaticnova";

    private PrismaticNovaAnimation() {
    }

    public static void launch(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }

        DamageConfig damageConfig = DamageUtils.getDamageConfig("prismaticnova", plugin);
        world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.6f, 1.2f);

        final int maxTicks = 34;
        final int[] tick = {0};
        final boolean[] exploded = {false};
        final SchedulerWrapper.ScheduledTask[] taskRef = new SchedulerWrapper.ScheduledTask[1];

        taskRef[0] = SchedulerWrapper.runTaskTimerAtLocation(plugin, () -> {
            int currentTick = tick[0]++;
            if (currentTick > maxTicks) {
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
                return;
            }

            double radius = 0.35 + (currentTick * 0.12);
            double yOffset = (Math.sin(currentTick * 0.35) * 0.4) + 0.15;

            if (currentTick % plugin.getParticlePerformanceManager().scaleTickInterval(EFFECT_ID, 1L, true) == 0) {
                spawnRing(plugin, world, center.clone().add(0, yOffset, 0), radius, currentTick);
                spawnHelix(plugin, world, center, radius * 0.6, currentTick);
            }

            if (currentTick % 5 == 0) {
                world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 0.8f + (currentTick * 0.02f));
            }

            if (currentTick >= 28 && !exploded[0]) {
                exploded[0] = true;
                explode(plugin, world, center);
                if (damageConfig.isEnabled()) {
                    DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
                }
            }
        }, center, 0L, 1L);
    }

    private static void spawnRing(KT plugin, World world, Location center, double radius, int tick) {
        int points = plugin.getParticlePerformanceManager().scaleLoopCount(EFFECT_ID, 42, true);
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            float hue = (float) ((i / (double) points) + (tick * 0.03)) % 1f;
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f) & 0x00FFFFFF;
            Color color = Color.fromRGB(rgb);
            Particle.DustOptions dust = new Particle.DustOptions(color, 1.35f);

            world.spawnParticle(
                    Particle.DUST,
                    center.clone().add(x, 0, z),
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    dust
            );
        }
    }

    private static void spawnHelix(KT plugin, World world, Location center, double radius, int tick) {
        for (int arm = 0; arm < 2; arm++) {
            double phase = arm * Math.PI;
            int steps = plugin.getParticlePerformanceManager().scaleLoopCount(EFFECT_ID, 18, true);
            for (int step = 0; step < steps; step++) {
                double progress = step / (double) Math.max(1, steps - 1);
                double angle = (tick * 0.32) + (progress * 3.3 * Math.PI) + phase;
                double x = Math.cos(angle) * (radius * (0.4 + progress));
                double z = Math.sin(angle) * (radius * (0.4 + progress));
                double y = progress * 2.2;
                world.spawnParticle(Particle.END_ROD, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
            }
        }
    }

    private static void explode(KT plugin, World world, Location center) {
        world.spawnParticle(
                Particle.FIREWORK,
                center,
                plugin.getParticlePerformanceManager().scaleParticleCount(EFFECT_ID, 120, true),
                1.2,
                1.2,
                1.2,
                0.05
        );
        world.spawnParticle(
                Particle.WAX_ON,
                center,
                plugin.getParticlePerformanceManager().scaleParticleCount(EFFECT_ID, 80, true),
                1.0,
                0.8,
                1.0,
                0.1
        );
        world.spawnParticle(
                Particle.END_ROD,
                center,
                plugin.getParticlePerformanceManager().scaleParticleCount(EFFECT_ID, 40, true),
                0.7,
                0.7,
                0.7,
                0.05
        );
        world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 2.0f, 1.0f);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.35f);
    }
}
