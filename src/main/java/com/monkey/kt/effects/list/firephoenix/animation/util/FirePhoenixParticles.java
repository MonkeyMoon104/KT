package com.monkey.kt.effects.list.firephoenix.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FirePhoenixParticles {

    public static void spawnPhoenixExplosion(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        ConfigurationSection effectConfig = plugin.getConfig().getConfigurationSection("effects.firephoenix");
        boolean projectilesEnabled = effectConfig.getBoolean("projectiles.enabled", false);

        boolean projectilesDamage = false;
        int projectilesRange = 200;
        int projectilesDelay = 3;
        double projectilesValue = 4.0;

        if (projectilesEnabled) {
            ConfigurationSection projectilesSettings = effectConfig.getConfigurationSection("projectiles.settings");
            if (projectilesSettings != null) {
                projectilesDamage = projectilesSettings.getBoolean("damage", true);
                projectilesRange = projectilesSettings.getInt("rage", 200);
                projectilesDelay = projectilesSettings.getInt("delay", 3);
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
        final int finalProjectilesDelay = projectilesDelay;
        final double finalProjectilesValue = projectilesValue;

        new BukkitRunnable() {
            int step = 0;
            int projectileTimer = 0;
            final Color[] phoenixColors = {
                    Color.fromRGB(255, 40, 0),
                    Color.fromRGB(255, 120, 0),
                    Color.fromRGB(255, 220, 50),
                    Color.fromRGB(255, 180, 30)
            };

            @Override
            public void run() {
                Vector directionToKiller = killer.getLocation().toVector().subtract(center.toVector()).normalize();
                double yawToKiller = Math.atan2(-directionToKiller.getX(), directionToKiller.getZ()) + Math.PI;
                double pitchToKiller = Math.asin(-directionToKiller.getY());
                if (step > 240) {
                    world.spawnParticle(Particle.FLASH, center, 3);
                    world.spawnParticle(Particle.EXPLOSION, center, 5, 2, 2, 2, 0);

                    for (int i = 0; i < 150; i++) {
                        double ox = (Math.random() - 0.5) * 6;
                        double oy = Math.random() * 3;
                        double oz = (Math.random() - 0.5) * 6;
                        world.spawnParticle(Particle.FALLING_LAVA,
                                center.clone().add(ox, oy + 2, oz),
                                0, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME,
                                center.clone().add(ox * 0.5, oy + 1, oz * 0.5),
                                0, 0, 0, 0, 0.1);
                    }
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);
                    cancel();
                    return;
                }

                double forwardTilt = Math.toRadians(25) + pitchToKiller;
                double phoenixMove = Math.sin(step * 0.1) * 0.3;

                for (double y = 0; y <= 4; y += 0.12) {
                    double bodyRadius = 0.6 + Math.sin((y / 4) * Math.PI) * 0.4;
                    double tiltedY = y * Math.cos(forwardTilt);
                    double tiltedZ = -y * Math.sin(forwardTilt);

                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 12) {
                        double x = Math.cos(angle) * bodyRadius;
                        double z = Math.sin(angle) * bodyRadius + tiltedZ;

                        double rotatedX = x * Math.cos(yawToKiller) - z * Math.sin(yawToKiller);
                        double rotatedZ = x * Math.sin(yawToKiller) + z * Math.cos(yawToKiller);

                        spawnDust(world, center.clone().add(rotatedX, tiltedY + phoenixMove, rotatedZ),
                                phoenixColors[step % phoenixColors.length], 1.6f);

                        if (Math.random() < 0.1) {
                            world.spawnParticle(Particle.FLAME,
                                    center.clone().add(rotatedX, tiltedY + phoenixMove, rotatedZ),
                                    0, 0.05, 0, 0, 0.02);
                        }
                    }
                }

                double headY = 4.5 * Math.cos(forwardTilt) + phoenixMove;
                double headZ = -4.5 * Math.sin(forwardTilt);

                double rotatedHeadX = 0 * Math.cos(yawToKiller) - headZ * Math.sin(yawToKiller);
                double rotatedHeadZ = 0 * Math.sin(yawToKiller) + headZ * Math.cos(yawToKiller);

                Location headPos = center.clone().add(rotatedHeadX, headY, rotatedHeadZ);

                spawnSphere(world, headPos, 0.4, Particle.FLAME);
                spawnSphere(world, headPos, 0.25, phoenixColors[0], 1.8f);

                if (finalProjectilesEnabled) {
                    if (projectileTimer <= 0) {
                        for (Player p : world.getPlayers()) {
                            if (!p.equals(killer) && p.getLocation().distance(center) <= finalProjectilesRange) {
                                FirePhoenixProjectiles.launchHomingFireCharge(plugin, headPos, p, finalProjectilesDamage, finalProjectilesValue);
                            }
                        }
                        projectileTimer = finalProjectilesDelay * 20;
                    } else {
                        projectileTimer--;
                    }
                }

                double beakX = 0 * Math.cos(yawToKiller) - (-0.4) * Math.sin(yawToKiller);
                double beakZ = 0 * Math.sin(yawToKiller) + (-0.4) * Math.cos(yawToKiller);

                world.spawnParticle(Particle.DUST,
                        headPos.clone().add(beakX, 0, beakZ),
                        0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(200, 100, 0), 1.0f));

                for (int i = 0; i < 5; i++) {
                    double crownX = (Math.random() - 0.5) * 0.6;
                    double crownZ = (Math.random() - 0.5) * 0.4;

                    double rotatedCrownX = crownX * Math.cos(yawToKiller) - crownZ * Math.sin(yawToKiller);
                    double rotatedCrownZ = crownX * Math.sin(yawToKiller) + crownZ * Math.cos(yawToKiller);

                    world.spawnParticle(Particle.FLAME,
                            headPos.clone().add(rotatedCrownX, 0.3 + Math.random() * 0.8, rotatedCrownZ),
                            0, 0, 0, 0, 0.01);
                }

                double wingBeatSpeed = step * 0.25;
                double wingAmplitude = 1.0;

                for (int side = -1; side <= 1; side += 2) {
                    for (double wingSection = 0; wingSection <= 1; wingSection += 0.08) {
                        for (double wingLength = 0.5; wingLength < 7; wingLength += 0.3) {

                            double wavePhase = wingBeatSpeed - (wingLength * 0.4);
                            double wingBeat = Math.sin(wavePhase) * wingAmplitude;
                            double amplitudeReduction = 1.0 - (wingLength / 7.0) * 0.3;
                            wingBeat *= amplitudeReduction;

                            double wingY = 2 + wingBeat;
                            double wingTiltY = wingY * Math.cos(forwardTilt);
                            double wingTiltZ = -wingY * Math.sin(forwardTilt) * 0.3;

                            double wingCurve = Math.sin(wingSection * Math.PI) * 2;
                            double wingX = side * (wingLength + wingCurve);
                            double wingZ = wingTiltZ + Math.cos(wingLength / 7 * Math.PI) * 1.2;

                            double rotatedWingX = wingX * Math.cos(yawToKiller) - wingZ * Math.sin(yawToKiller);
                            double rotatedWingZ = wingX * Math.sin(yawToKiller) + wingZ * Math.cos(yawToKiller);

                            double featherMovement = Math.sin(wavePhase + wingSection * Math.PI) * 0.15;
                            double featherY = wingTiltY + featherMovement;

                            Location wingPos = center.clone().add(rotatedWingX, featherY, rotatedWingZ);
                            spawnDust(world, wingPos,
                                    phoenixColors[(step + (int)(wingLength * 3)) % phoenixColors.length], 1.4f);

                            double wingVelocity = Math.abs(Math.cos(wavePhase));
                            if (wingVelocity > 0.7 && Math.random() < 0.2) {
                                world.spawnParticle(Particle.FLAME, wingPos, 0, 0, 0, 0, 0.02);
                            }

                            if (wingLength > 5 && wingBeat < -0.3 && Math.random() < 0.15) {
                                world.spawnParticle(Particle.ENCHANTED_HIT, wingPos, 0, 0, 0, 0, 0);
                                world.spawnParticle(Particle.DUST, wingPos,
                                        0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(255, 215, 0), 0.8f));
                            }
                        }
                    }
                }

                for (double t = 0; t < Math.PI; t += Math.PI / 20) {
                    for (double r = 0.3; r < 4; r += 0.2) {
                        double tailX = Math.cos(t) * r * 0.9;
                        double tailZ = Math.sin(t) * r - r * 0.8;
                        double tailY = -r * 0.4 + Math.sin(step * 0.15 + r) * 0.2;

                        double tiltedTailY = tailY * Math.cos(forwardTilt) - tailZ * Math.sin(forwardTilt);
                        double tiltedTailZ = tailY * Math.sin(forwardTilt) + tailZ * Math.cos(forwardTilt);

                        double rotatedTailX = tailX * Math.cos(yawToKiller) - tiltedTailZ * Math.sin(yawToKiller);
                        double rotatedTailZ = tailX * Math.sin(yawToKiller) + tiltedTailZ * Math.cos(yawToKiller);

                        Location tailPos = center.clone().add(rotatedTailX, tiltedTailY, rotatedTailZ);
                        spawnDust(world, tailPos,
                                phoenixColors[(step + (int)(r * 15)) % phoenixColors.length], 1.2f);

                        if (Math.random() < 0.12) {
                            world.spawnParticle(Particle.FLAME, tailPos, 0, 0, 0, 0, 0.01);
                        }
                    }
                }

                if (step > 10) {
                    for (int i = 0; i < 8; i++) {
                        double trailX = (Math.random() - 0.5) * 2;
                        double trailY = (Math.random() - 0.5) * 6;
                        double trailZ = (Math.random() - 0.5) * 2;

                        double rotatedTrailX = trailX * Math.cos(yawToKiller) - trailZ * Math.sin(yawToKiller);
                        double rotatedTrailZ = trailX * Math.sin(yawToKiller) + trailZ * Math.cos(yawToKiller);

                        world.spawnParticle(Particle.FLAME,
                                center.clone().add(rotatedTrailX, trailY, rotatedTrailZ),
                                0, 0, 0, 0, 0.05);
                    }
                }

                step++;
            }

            private void spawnDust(World world, Location loc, Color color, float size) {
                world.spawnParticle(Particle.DUST, loc, 0, 0, 0, 0, new Particle.DustOptions(color, size));
            }

            private void spawnSphere(World world, Location center, double radius, Color color, float size) {
                for (double phi = 0; phi < Math.PI; phi += Math.PI / 8) {
                    for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 8) {
                        double x = radius * Math.sin(phi) * Math.cos(theta);
                        double y = radius * Math.cos(phi);
                        double z = radius * Math.sin(phi) * Math.sin(theta);
                        world.spawnParticle(Particle.DUST, center.clone().add(x, y, z),
                                0, 0, 0, 0, new Particle.DustOptions(color, size));
                    }
                }
            }

            private void spawnSphere(World world, Location center, double radius, Particle type) {
                for (double phi = 0; phi < Math.PI; phi += Math.PI / 8) {
                    for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 8) {
                        double x = radius * Math.sin(phi) * Math.cos(theta);
                        double y = radius * Math.cos(phi);
                        double z = radius * Math.sin(phi) * Math.sin(theta);
                        world.spawnParticle(type, center.clone().add(x, y, z), 0, 0, 0, 0, 0.01);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}