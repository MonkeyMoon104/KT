package com.monkey.kt.effects.list.firephoenix.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FirePhoenixParticles {

    public static void spawnPhoenixExplosion(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        ConfigurationSection effectConfig = plugin.getConfig().getConfigurationSection("effects.firephoenix");
        boolean projectilesEnabled = effectConfig != null && effectConfig.getBoolean("projectiles.enabled", false);

        boolean projectilesDamage = false;
        int projectilesRange = 24;
        int maxTargets = 2;
        double projectilesValue = 4.0;

        if (projectilesEnabled) {
            ConfigurationSection projectilesSettings = effectConfig.getConfigurationSection("projectiles.settings");
            if (projectilesSettings != null) {
                projectilesDamage = projectilesSettings.getBoolean("damage", true);
                projectilesRange = clamp(projectilesSettings.getInt("rage", 24), 6, 32);
                maxTargets = clamp(projectilesSettings.getInt("max_targets", 2), 1, 4);
                projectilesValue = projectilesSettings.getDouble("value", 4.0);
            }
        }

        DamageConfig damageConfig = DamageUtils.getDamageConfig("firephoenix", plugin);
        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
        }

        final boolean finalProjectilesEnabled = projectilesEnabled;
        final boolean finalProjectilesDamage = projectilesDamage;
        final int finalProjectilesRange = projectilesRange;
        final int finalMaxTargets = maxTargets;
        final double finalProjectilesValue = projectilesValue;

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            int step = 0;
            boolean projectileBurstTriggered = false;
            final Color[] phoenixColors = {
                    Color.fromRGB(255, 40, 0),
                    Color.fromRGB(255, 120, 0),
                    Color.fromRGB(255, 220, 50),
                    Color.fromRGB(255, 180, 30)
            };

            @Override
            public void run() {
                if (taskCompleted[0]) return;

                try {
                    if (step > 14) {
                        spawnFinalBurst(plugin, world, center);
                        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.3f, 1.0f);
                        SchedulerWrapper.safeCancelTask(this);
                        taskCompleted[0] = true;
                        return;
                    }

                    double rise = step * 0.16;
                    double wingBeat = Math.sin(step * 0.8) * 0.5;
                    Color bodyColor = phoenixColors[step % phoenixColors.length];
                    Location bodyBase = center.clone().add(0, 0.35 + rise, 0);

                    for (int i = 0; i < 5; i++) {
                        Location bodyPoint = bodyBase.clone().add(0, i * 0.22, -i * 0.05);
                        spawnDust(plugin, world, bodyPoint, bodyColor, 1.15f);
                        if (i % 2 == 0) {
                            safeSpawn(plugin, world, Particle.FLAME, bodyPoint, 1, 0.03, 0.03, 0.03, 0.01);
                        }
                    }

                    Location headPos = bodyBase.clone().add(0, 1.15, -0.15);
                    spawnDust(plugin, world, headPos, phoenixColors[0], 1.25f);
                    safeSpawn(plugin, world, Particle.FLAME, headPos, 2, 0.04, 0.04, 0.04, 0.01);

                    for (int side = -1; side <= 1; side += 2) {
                        for (int segment = 0; segment < 6; segment++) {
                            double spread = 0.45 + (segment * 0.33);
                            double lift = 0.45 + Math.abs(wingBeat) * 0.45 - (segment * 0.03);
                            double sway = Math.sin(step * 0.5 + segment * 0.55) * 0.08;

                            Location wingPos = bodyBase.clone().add(
                                    side * spread,
                                    lift + sway,
                                    -0.1 - (segment * 0.1)
                            );

                            spawnDust(plugin, world, wingPos, phoenixColors[(step + segment) % phoenixColors.length], 1.0f);
                            if (segment >= 3) {
                                safeSpawn(plugin, world, Particle.FLAME, wingPos, 1, 0.02, 0.02, 0.02, 0.01);
                            }
                        }
                    }

                    for (int tail = 0; tail < 4; tail++) {
                        Location tailPos = bodyBase.clone().add(
                                (Math.random() - 0.5) * 0.35,
                                -0.15 - (tail * 0.12),
                                0.12 + (tail * 0.18)
                        );
                        spawnDust(plugin, world, tailPos, phoenixColors[(step + tail) % phoenixColors.length], 0.9f);
                    }

                    if (finalProjectilesEnabled && !projectileBurstTriggered && step >= 7) {
                        launchProjectileBurst(plugin, killer, world, headPos, center, finalProjectilesRange, finalMaxTargets,
                                finalProjectilesDamage, finalProjectilesValue);
                        projectileBurstTriggered = true;
                    }

                    step++;
                } catch (Exception ex) {
                    plugin.getLogger().warning("FirePhoenix animation stopped due to particle error: " + ex.getMessage());
                    SchedulerWrapper.safeCancelTask(this);
                    taskCompleted[0] = true;
                }
            }

        }, center, 0L, 2L);
    }

    private static void launchProjectileBurst(KT plugin, Player killer, World world, Location headPos, Location center,
                                              int range, int maxTargets, boolean shouldDamage, double damageValue) {
        List<Player> targets = new ArrayList<>(world.getPlayers());
        targets.removeIf(player -> player.equals(killer) || player.isDead()
                || player.getLocation().distance(center) > range);
        targets.sort(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(center)));

        int launched = 0;
        for (Player target : targets) {
            FirePhoenixProjectiles.launchHomingFireCharge(plugin, headPos, target, shouldDamage, damageValue);
            launched++;
            if (launched >= maxTargets) {
                break;
            }
        }
    }

    private static void spawnFinalBurst(KT plugin, World world, Location center) {
        safeSpawn(plugin, world, Particle.FLASH, center, 1, 0, 0, 0, 0);
        safeSpawn(plugin, world, Particle.EXPLOSION, center, 2, 0.35, 0.35, 0.35, 0.01);
        safeSpawn(plugin, world, Particle.FLAME, center, 18, 0.8, 0.8, 0.8, 0.03);
        safeSpawn(plugin, world, Particle.LAVA, center.clone().add(0, 0.5, 0), 8, 0.25, 0.2, 0.25, 0.01);
        safeSpawn(plugin, world, Particle.ENCHANTED_HIT, center, 10, 0.5, 0.4, 0.5, 0.02);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void spawnDust(KT plugin, World world, Location loc, Color color, float size) {
        try {
            world.spawnParticle(Particle.DUST, loc, 0, 0, 0, 0, new Particle.DustOptions(color, size));
        } catch (Exception ex) {
            plugin.getLogger().warning("Skipping FirePhoenix DUST particle due to error: " + ex.getMessage());
        }
    }

    private static void safeSpawn(KT plugin, World world, Particle particle, Location location, int count,
                                  double offsetX, double offsetY, double offsetZ, double extra) {
        if (world == null || location == null) {
            return;
        }

        try {
            if (particle.getDataType() != Void.class) {
                plugin.getLogger().warning("Skipping FirePhoenix particle " + particle.name()
                        + " because it requires data type " + particle.getDataType().getSimpleName());
                return;
            }

            world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);
        } catch (Exception ex) {
            plugin.getLogger().warning("Skipping FirePhoenix particle " + particle.name() + " due to error: " + ex.getMessage());
        }
    }
}
