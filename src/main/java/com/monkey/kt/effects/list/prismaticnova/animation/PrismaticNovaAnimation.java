package com.monkey.kt.effects.list.prismaticnova.animation;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class PrismaticNovaAnimation {

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

            spawnRing(world, center.clone().add(0, yOffset, 0), radius, currentTick);
            spawnHelix(world, center, radius * 0.6, currentTick);

            if (currentTick % 5 == 0) {
                world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 0.8f + (currentTick * 0.02f));
            }

            if (currentTick >= 28 && !exploded[0]) {
                exploded[0] = true;
                explode(world, center);
                if (damageConfig.isEnabled()) {
                    DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
                }
            }
        }, center, 0L, 1L);
    }

    private static void spawnRing(World world, Location center, double radius, int tick) {
        int points = 42;
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            float hue = (float) ((i / (double) points) + (tick * 0.03)) % 1f;
            Color color = Color.fromRGB(java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f));
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

    private static void spawnHelix(World world, Location center, double radius, int tick) {
        for (int arm = 0; arm < 2; arm++) {
            double phase = arm * Math.PI;
            for (int step = 0; step < 18; step++) {
                double progress = step / 17.0;
                double angle = (tick * 0.32) + (progress * 3.3 * Math.PI) + phase;
                double x = Math.cos(angle) * (radius * (0.4 + progress));
                double z = Math.sin(angle) * (radius * (0.4 + progress));
                double y = progress * 2.2;
                world.spawnParticle(Particle.END_ROD, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
            }
        }
    }

    private static void explode(World world, Location center) {
        world.spawnParticle(Particle.FIREWORK, center, 120, 1.2, 1.2, 1.2, 0.05);
        world.spawnParticle(Particle.WAX_ON, center, 80, 1.0, 0.8, 1.0, 0.1);
        world.spawnParticle(Particle.END_ROD, center, 40, 0.7, 0.7, 0.7, 0.05);
        world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 2.0f, 1.0f);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.35f);
    }
}
