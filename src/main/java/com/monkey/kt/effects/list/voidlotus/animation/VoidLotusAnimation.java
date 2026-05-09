package com.monkey.kt.effects.list.voidlotus.animation;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Player;

public final class VoidLotusAnimation {
    private static final String EFFECT_ID = "voidlotus";

    private VoidLotusAnimation() {
    }

    public static void launch(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }

        DamageConfig damageConfig = DamageUtils.getDamageConfig("voidlotus", plugin);
        world.playSound(center, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.2f, 0.8f);

        final int[] tick = {0};
        final int maxTicks = 38;
        final boolean[] damaged = {false};
        final SchedulerWrapper.ScheduledTask[] taskRef = new SchedulerWrapper.ScheduledTask[1];

        taskRef[0] = SchedulerWrapper.runTaskTimerAtLocation(plugin, () -> {
            int currentTick = tick[0]++;
            if (currentTick > maxTicks) {
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
                return;
            }

            if (currentTick % plugin.getParticlePerformanceManager().scaleTickInterval(EFFECT_ID, 1L, true) == 0) {
                spawnLotusPetals(plugin, world, center, currentTick);
                spawnCore(plugin, world, center, currentTick);
            }

            if (currentTick == 18) {
                world.playSound(center, Sound.ENTITY_ALLAY_DEATH, 1.3f, 0.65f);
            }

            if (currentTick == 30) {
                world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.85f, 1.5f);
            }

            if (currentTick == 34) {
                implode(plugin, world, center);
                if (!damaged[0] && damageConfig.isEnabled()) {
                    damaged[0] = true;
                    DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
                }
            }
        }, center, 0L, 1L);
    }

    private static void spawnLotusPetals(KT plugin, World world, Location center, int tick) {
        int petals = plugin.getParticlePerformanceManager().scaleLoopCount(EFFECT_ID, 8, true);
        double baseRadius = Math.min(3.2, 0.4 + (tick * 0.08));
        double wave = Math.sin(tick * 0.28) * 0.35;

        for (int i = 0; i < petals; i++) {
            double petalAngle = (Math.PI * 2 * i) / petals + (tick * 0.06);
            int steps = plugin.getParticlePerformanceManager().scaleLoopCount(EFFECT_ID, 14, true);
            for (int step = 0; step < steps; step++) {
                double progress = step / (double) Math.max(1, steps - 1);
                double petalWidth = Math.sin(progress * Math.PI) * 0.65;
                double radius = baseRadius + (petalWidth * 0.75);
                double x = Math.cos(petalAngle) * radius;
                double z = Math.sin(petalAngle) * radius;
                double y = 0.12 + (progress * 1.25) + wave;

                Color dustColor = step % 2 == 0
                        ? Color.fromRGB(186, 104, 255)
                        : Color.fromRGB(98, 74, 255);

                world.spawnParticle(
                        Particle.DUST,
                        center.clone().add(x, y, z),
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new Particle.DustOptions(dustColor, 1.2f)
                );
            }
        }
    }

    private static void spawnCore(KT plugin, World world, Location center, int tick) {
        double spin = tick * 0.21;
        int layers = plugin.getParticlePerformanceManager().scaleLoopCount(EFFECT_ID, 3, true);
        for (int i = 0; i < layers; i++) {
            double angle = spin + (i * 2.1);
            double x = Math.cos(angle) * 0.45;
            double z = Math.sin(angle) * 0.45;
            double y = 0.2 + (i * 0.35);
            world.spawnParticle(
                    Particle.DRAGON_BREATH,
                    center.clone().add(x, y, z),
                    plugin.getParticlePerformanceManager().scaleParticleCount(EFFECT_ID, 2, true),
                    0.05,
                    0.05,
                    0.05,
                    0.01
            );
            world.spawnParticle(
                    Particle.PORTAL,
                    center.clone().add(-x, y + 0.2, -z),
                    plugin.getParticlePerformanceManager().scaleParticleCount(EFFECT_ID, 2, true),
                    0.1,
                    0.1,
                    0.1,
                    0.02
            );
        }
    }

    private static void implode(KT plugin, World world, Location center) {
        world.spawnParticle(
                Particle.REVERSE_PORTAL,
                center,
                plugin.getParticlePerformanceManager().scaleParticleCount(EFFECT_ID, 110, true),
                1.5,
                1.5,
                1.5,
                0.2
        );
        world.spawnParticle(Particle.SONIC_BOOM, center, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticle(
                Particle.SCULK_SOUL,
                center,
                plugin.getParticlePerformanceManager().scaleParticleCount(EFFECT_ID, 35, true),
                1.1,
                0.9,
                1.1,
                0.08
        );
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.6f);
        world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.35f, 1.1f);
    }
}
