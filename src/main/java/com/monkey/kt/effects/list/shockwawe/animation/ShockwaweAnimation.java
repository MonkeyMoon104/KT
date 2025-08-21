package com.monkey.kt.effects.list.shockwawe.animation;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class ShockwaweAnimation {

    private final KT plugin;
    private final World world;
    private final Location center;

    public ShockwaweAnimation(KT plugin, World world, Location center) {
        this.plugin = plugin;
        this.world = world;
        this.center = center;
    }

    public void start(Player killer) {
        final int[] ticks = {0};
        Location current = center.clone();
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask riseTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;
                if (ticks[0] > 20) {
                    taskCompleted[0] = true;
                    spawnSphere(killer, current.clone().add(0, 0.5, 0));
                    return;
                }

                current.add(0, 0.5, 0);
                world.spawnParticle(
                        Particle.DUST, current, 5,
                        0.05, 0.05, 0.05, 0.01,
                        new Particle.DustOptions(Color.fromRGB(56, 189, 255), 1.5f)
                );

                ticks[0]++;
            }
        }, center, 0L, 1L);
    }

    private void spawnSphere(Player killer, Location top) {
        final int[] ticks = {0};
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask sphereTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;
                if (ticks[0] > 20) {
                    taskCompleted[0] = true;
                    fallDown(killer, top);
                    return;
                }

                double radius = 0.5;
                for (double phi = 0; phi < Math.PI; phi += Math.PI / 8) {
                    for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 8) {
                        double x = radius * Math.sin(phi) * Math.cos(theta);
                        double y = radius * Math.cos(phi);
                        double z = radius * Math.sin(phi) * Math.sin(theta);

                        Location loc = top.clone().add(x, y, z);
                        world.spawnParticle(
                                Particle.DUST, loc, 1,
                                0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(56, 189, 255), 2f)
                        );
                    }
                }

                ticks[0]++;
            }
        }, top, 0L, 2L);
    }

    private void fallDown(Player killer, Location start) {
        Location current = start.clone();
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask fallTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;
                if (current.getY() <= center.getY()) {
                    taskCompleted[0] = true;
                    playShockwave(killer, center);
                    return;
                }

                current.subtract(0, 0.5, 0);
                world.spawnParticle(
                        Particle.DUST, current, 6,
                        0.1, 0.1, 0.1, 0.02,
                        new Particle.DustOptions(Color.fromRGB(56, 189, 255), 1.7f)
                );
            }
        }, start, 0L, 1L);
    }

    private void playShockwave(Player killer, Location center) {
        DamageConfig damageConfig = DamageUtils.getDamageConfig("shockwawe", plugin);
        boolean effectEnabled = plugin.getConfig().getBoolean("shockwawe.effectexplosion.enabled", true);
        String typeName = plugin.getConfig().getString("shockwawe.effectexplosion.type", "BLINDNESS");
        int amplifier = plugin.getConfig().getInt("shockwawe.effectexplosion.amplifier", 1);
        int duration = plugin.getConfig().getInt("shockwawe.effectexplosion.duration", 3);

        PotionEffectType effectType = PotionEffectType.getByName(typeName.toUpperCase());
        if (effectType == null) effectType = PotionEffectType.BLINDNESS;

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.2f);

        PotionEffectType finalEffectType = effectType;
        final double[] radius = {0};
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask shockwaveTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;
                if (radius[0] > damageConfig.getRadius()) {
                    taskCompleted[0] = true;
                    return;
                }

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius[0];
                    double z = Math.sin(angle) * radius[0];
                    Location loc = center.clone().add(x, 0.1, z);

                    world.spawnParticle(
                            Particle.DUST, loc, 2,
                            0.05, 0.05, 0.05, 0.01,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 255), 2f)
                    );
                }

                double innerRadius = radius[0] * 0.7;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * innerRadius;
                    double z = Math.sin(angle) * innerRadius;
                    Location loc = center.clone().add(x, 0.1, z);

                    world.spawnParticle(
                            Particle.DUST, loc, 2,
                            0.05, 0.05, 0.05, 0.01,
                            new Particle.DustOptions(Color.fromRGB(56, 189, 255), 2f)
                    );
                }

                if (effectEnabled) {
                    for (Player p : world.getPlayers()) {
                        if (p.getLocation().distance(center) <= radius[0]) {
                            p.addPotionEffect(finalEffectType.createEffect(duration * 20, amplifier - 1));
                        }
                    }
                }

                if (damageConfig.isEnabled()) {
                    for (Player p : world.getPlayers()) {
                        if (p.getLocation().distance(center) <= damageConfig.getRadius()) {
                            p.damage(damageConfig.getValue(), killer);
                        }
                    }
                }

                radius[0] += 0.5;
            }
        }, center, 0L, 2L);
    }
}