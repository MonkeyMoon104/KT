package com.monkey.kt.effects.list.explosion.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.FixedMetadataValue;
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

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAtEntity(plugin, new Runnable() {
            private int tickCount = 0;

            @Override
            public void run() {
                if (taskCompleted[0]) return;

                tickCount++;

                if (!tnt.isValid() || tnt.isDead() || tickCount > 200) {
                    taskCompleted[0] = true;

                    if (tnt.hasMetadata("kt_explosion_task")) {
                        SchedulerWrapper.safeCancelTask(tnt.getMetadata("kt_explosion_task").get(0).value());
                    }

                    handleExplosion(plugin, tnt);
                    return;
                }

                Location loc = tnt.getLocation();
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.1, 0.1, 0.1, 0.05);
                    loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 2, 0.1, 0.1, 0.1, 0.02);
                    loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 2, 0.1, 0.1, 0.1, 0.01);
                }
            }
        }, tnt, 0L, 1L);

        if (task != null) {
            tnt.setMetadata("kt_explosion_task", new FixedMetadataValue(plugin, task));
        }
    }

    private static void handleExplosion(KT plugin, TNTPrimed tnt) {
        Location explosionLoc = tnt.getLocation();
        World world = explosionLoc.getWorld();
        if (world == null) return;

        if (tnt.hasMetadata("kt_explosion_task")) {
            tnt.getMetadata("kt_explosion_task").forEach(meta -> {
                SchedulerWrapper.safeCancelTask(meta.value());
            });
        }

        spawnExplosionParticles(world, explosionLoc);
        world.playSound(explosionLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 1.0f);

        boolean damageEnabled = tnt.hasMetadata("kt_explosion_damage_enabled") ?
                tnt.getMetadata("kt_explosion_damage_enabled").get(0).asBoolean() : false;

        if (damageEnabled) {
            double damageValue = tnt.hasMetadata("kt_explosion_damage_value") ?
                    tnt.getMetadata("kt_explosion_damage_value").get(0).asDouble() : 2.0;
            double rage = tnt.hasMetadata("kt_explosion_damage_rage") ?
                    tnt.getMetadata("kt_explosion_damage_rage").get(0).asDouble() : 5.0;

            Player killer = null;
            if (tnt.hasMetadata("kt_explosion_killer")) {
                try {
                    String killerUUID = tnt.getMetadata("kt_explosion_killer").get(0).asString();
                    killer = Bukkit.getPlayer(java.util.UUID.fromString(killerUUID));
                } catch (Exception e) {
                }
            }

            final Player finalKiller = killer;
            SchedulerWrapper.runTaskAtLocation(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        for (Entity entity : world.getNearbyEntities(explosionLoc, rage, rage, rage)) {
                            if (entity.equals(finalKiller)) continue;
                            if (entity instanceof LivingEntity) {
                                ((LivingEntity) entity).damage(damageValue, finalKiller != null ? finalKiller : tnt);
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Errore durante l'applicazione del danno esplosione: " + e.getMessage());
                    }
                }
            }, explosionLoc, 0L);
        }

        if (tnt.isValid() && !tnt.isDead()) {
            tnt.remove();
        }
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