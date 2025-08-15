package com.monkey.kt.effects.list.explosion.animation.util;

import com.monkey.kt.KT;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class ExplosionUtils {

    private static final Random random = new Random();

    public static void launchCosmeticTNT(final KT plugin, final Location center, Player killer) {
        final World world = center.getWorld();
        if (world == null) return;

        final TNTPrimed tnt = world.spawn(center.clone().add(0, 1, 0), TNTPrimed.class);
        tnt.setFuseTicks(40 + random.nextInt(20));
        tnt.setInvulnerable(true);
        tnt.setSilent(true);
        tnt.setYield(0f);
        tnt.setMetadata("kt_explosion_tnt", new FixedMetadataValue(plugin, true));

        boolean damageEnabled = plugin.getConfig().getBoolean("effects.explosion.projectiles.damage", true);
        int rage = plugin.getConfig().getInt("effects.explosion.projectiles.settings.rage", 5);
        double value = plugin.getConfig().getDouble("effects.explosion.projectiles.settings.value", 2);

        tnt.setMetadata("kt_explosion_damage_enabled", new FixedMetadataValue(plugin, damageEnabled));
        tnt.setMetadata("kt_explosion_damage_rage", new FixedMetadataValue(plugin, rage));
        tnt.setMetadata("kt_explosion_damage_value", new FixedMetadataValue(plugin, value));
        tnt.setMetadata("kt_explosion_killer", new FixedMetadataValue(plugin, killer.getUniqueId().toString()));


        Vector vel = new Vector(
                (random.nextDouble() - 0.5) * 1.5,
                0.8 + random.nextDouble() * 0.5,
                (random.nextDouble() - 0.5) * 1.5
        );
        tnt.setVelocity(vel);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!tnt.isValid() || tnt.isDead()) {
                    cancel();
                    spawnExplosionParticles(world, tnt.getLocation());
                    world.playSound(tnt.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 1.0f);

                    boolean damageEnabled = tnt.getMetadata("kt_explosion_damage_enabled").get(0).asBoolean();
                    if (damageEnabled) {
                        double damageValue = tnt.getMetadata("kt_explosion_damage_value").get(0).asDouble();
                        double rage = tnt.getMetadata("kt_explosion_damage_rage").get(0).asDouble();
                        Player killer = null;
                        if (tnt.hasMetadata("kt_explosion_killer")) {
                            String killerUUID = tnt.getMetadata("kt_explosion_killer").get(0).asString();
                            killer = Bukkit.getPlayer(java.util.UUID.fromString(killerUUID));
                        }

                        for (Entity entity : world.getNearbyEntities(tnt.getLocation(), rage, rage, rage)) {
                            if (entity.equals(killer)) continue;
                            if (entity instanceof LivingEntity) {
                                ((LivingEntity) entity).damage(damageValue, killer != null ? killer : tnt);
                            }
                        }
                    }

                    return;
                }

                Location loc = tnt.getLocation();
                world.spawnParticle(Particle.FLAME, loc, 3, 0.1, 0.1, 0.1, 0.05);
                world.spawnParticle(Particle.LARGE_SMOKE, loc, 2, 0.1, 0.1, 0.1, 0.02);
                world.spawnParticle(Particle.ENCHANTED_HIT, loc, 2, 0.1, 0.1, 0.1, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void spawnExplosionParticles(World world, Location loc) {
        if (world == null) return;

        for (int layer = 0; layer < 4; layer++) {
            double radius = 0.5 + layer * 0.6;
            for (int i = 0; i < 16; i++) {
                double angle = 2 * Math.PI / 16 * i;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                double y = 0.2 * layer;

                Location particleLoc = loc.clone().add(x, y + 0.5, z);

                world.spawnParticle(Particle.FIREWORK, particleLoc, 1, 0, 0, 0, 0.01);
                world.spawnParticle(Particle.DRIPPING_LAVA, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);

                Location colorLoc = particleLoc.clone().add(0, Math.sin(angle * 4) * 0.3, 0);
                world.spawnParticle(Particle.DUST, colorLoc, 1, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 180 - layer * 30, 50 + layer * 40), 1.5f));

                if (i % 4 == 0) {
                    world.spawnParticle(Particle.LARGE_SMOKE, particleLoc.clone().add(0, 0.2, 0), 1, 0.05, 0.1, 0.05, 0.02);
                }
            }
        }
    }
}
