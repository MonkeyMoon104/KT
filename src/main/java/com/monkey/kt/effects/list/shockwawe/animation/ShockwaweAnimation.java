package com.monkey.kt.effects.list.shockwawe.animation;

import com.monkey.kt.KT;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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
        new BukkitRunnable() {
            int ticks = 0;
            Location current = center.clone();

            @Override
            public void run() {
                if (ticks > 20) {
                    cancel();
                    spawnSphere(killer, current.clone().add(0, 0.5, 0));
                    return;
                }

                current.add(0, 0.5, 0);
                world.spawnParticle(
                        Particle.DUST, current, 5,
                        0.05, 0.05, 0.05, 0.01,
                        new Particle.DustOptions(Color.fromRGB(56, 189, 255), 1.5f)
                );

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnSphere(Player killer, Location top) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 20) {
                    cancel();
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

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void fallDown(Player killer, Location start) {
        new BukkitRunnable() {
            Location current = start.clone();

            @Override
            public void run() {
                if (current.getY() <= center.getY()) {
                    cancel();
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
        }.runTaskTimer(plugin, 0L, 1L);
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
        new BukkitRunnable() {
            double radius = 0;

            @Override
            public void run() {
                if (radius > damageConfig.getRadius()) {
                    cancel();
                    return;
                }

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location loc = center.clone().add(x, 0.1, z);

                    world.spawnParticle(
                            Particle.DUST, loc, 2,
                            0.05, 0.05, 0.05, 0.01,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 255), 2f)
                    );
                }

                double innerRadius = radius * 0.7;
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
                        if (p.getLocation().distance(center) <= radius) {
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

                radius += 0.5;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

}

//Made by: Dominikhun250