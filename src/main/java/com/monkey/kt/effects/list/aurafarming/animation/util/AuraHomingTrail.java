package com.monkey.kt.effects.list.aurafarming.animation.util;

import com.monkey.kt.KT;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class AuraHomingTrail {

    private final KT plugin;
    private final Player killer;
    private final Player target;
    private final FileConfiguration config;

    private Location headPosition;
    private boolean connected = false;
    private int ticks = 0;
    private int damageTicks = 0;

    private final boolean pushEnabled;
    private final double damagePerSecond;

    private BukkitRunnable task;

    public AuraHomingTrail(KT plugin, Player killer, Player target) {
        this.plugin = plugin;
        this.killer = killer;
        this.target = target;
        this.config = plugin.getConfig();

        this.pushEnabled = config.getBoolean("effects.aurafarming.push-settings.enabled");
        this.damagePerSecond = config.getDouble("effects.aurafarming.push-settings.damage-per-seconds");
    }

    public void start() {
        headPosition = killer.getLocation().clone().add(0, 1.5, 0);

        task = new BukkitRunnable() {
            Vector controlPoint = headPosition.toVector();

            @Override
            public void run() {
                if (ticks > 180 || !target.isOnline() || target.isDead() || !killer.isOnline() || killer.isDead()) {
                    removeLevitation();
                    cancel();
                    return;
                }

                Location baseStart = killer.getLocation().clone().add(0, 1.5, 0);
                Vector start = baseStart.toVector().clone();

                Vector startOscillation = getOscillation(ticks, 0.25, 0.2);
                Vector dynamicStart = start.clone().add(startOscillation);

                Location end = target.getLocation().clone().add(0, 1.2, 0);

                if (!connected) {
                    Vector toTarget = end.toVector().subtract(headPosition.toVector());
                    double distance = toTarget.length();

                    if (distance < 1.5) {
                        connected = true;
                        damageTicks = 0;

                        if (pushEnabled) {
                            applyLevitation();
                        }
                    } else {
                        Vector direction = toTarget.clone().normalize().multiply(0.50);
                        Vector motion = direction
                                .add(getOscillation(ticks, 0.3, 0.15))
                                .add(new Vector(0, Math.cos(ticks * 0.1) * 0.2, 0));
                        headPosition.add(motion);
                    }
                } else {
                    if (pushEnabled) {
                        maintainLevitation();

                        damageTicks++;
                        if (damageTicks % 20 == 0) {
                            target.damage(damagePerSecond, killer);
                        }

                        pushTargetAway();
                    }
                }

                Vector midpoint = dynamicStart.clone().midpoint(connected ? end.toVector() : headPosition.toVector());
                controlPoint.add(midpoint.subtract(controlPoint).multiply(0.15));

                int segments = 28;
                for (int i = 0; i <= segments; i++) {
                    double t = i / (double) segments;

                    Vector bezierPoint = quadraticBezier(dynamicStart, controlPoint, connected ? end.toVector() : headPosition.toVector(), t);
                    Vector waveOffset = getOscillation(ticks + i * 2, 0.1 + (1 - Math.abs(t - 0.5)) * 0.3, 0.25);
                    bezierPoint.add(waveOffset);

                    Location loc = bezierPoint.toLocation(baseStart.getWorld());
                    spawnTrailParticle(loc);
                }

                ticks++;
            }
        };
        task.runTaskTimer(plugin, 0L, 1L);
    }

    private void applyLevitation() {
        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 99999, 0, false, false, false));
    }

    private void maintainLevitation() {
        if (!target.hasPotionEffect(PotionEffectType.LEVITATION)) {
            applyLevitation();
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 0, false, false, false));
        }
    }

    private void removeLevitation() {
        if (target.hasPotionEffect(PotionEffectType.LEVITATION)) {
            target.removePotionEffect(PotionEffectType.LEVITATION);
        }
    }

    private void pushTargetAway() {
        Vector pushDirection = target.getLocation().toVector().subtract(killer.getLocation().toVector()).normalize();
        Vector pushVelocity = pushDirection.multiply(0.15);

        pushVelocity.setY(0);

        target.setVelocity(pushVelocity);
    }

    private Vector quadraticBezier(Vector p0, Vector p1, Vector p2, double t) {
        Vector a = p0.clone().multiply(1 - t).add(p1.clone().multiply(t));
        Vector b = p1.clone().multiply(1 - t).add(p2.clone().multiply(t));
        return a.multiply(1 - t).add(b.multiply(t));
    }

    private void spawnTrailParticle(Location loc) {
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
        loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(170, 230, 255), 1.5f));
    }

    private Vector getOscillation(double time, double strength, double freq) {
        return new Vector(
                Math.sin(time * freq) * strength,
                Math.cos(time * freq * 0.5) * strength * 0.6,
                Math.cos(time * freq) * strength
        );
    }
}