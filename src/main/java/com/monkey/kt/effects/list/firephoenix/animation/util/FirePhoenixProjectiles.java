package com.monkey.kt.effects.list.firephoenix.animation.util;

import com.monkey.kt.KT;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FirePhoenixProjectiles {

    public static void launchHomingFireCharge(KT plugin, Location beakLocation, Player target, boolean shouldDamage, double damageValue) {
        World world = beakLocation.getWorld();
        if (world == null) return;

        Vector dir = target.getEyeLocation().toVector().subtract(beakLocation.toVector()).normalize();
        SmallFireball fireball = world.spawn(beakLocation, SmallFireball.class);
        fireball.setDirection(dir);
        fireball.setVelocity(dir.multiply(0.5));
        fireball.setIsIncendiary(false);
        fireball.setYield(0);
        fireball.setSilent(true);

        fireball.setMetadata("kt_phoenix_firecharge", new FixedMetadataValue(plugin, true));

        if (shouldDamage) {
            fireball.setMetadata("kt_phoenix_damage_enabled", new FixedMetadataValue(plugin, true));
            fireball.setMetadata("kt_phoenix_damage_value", new FixedMetadataValue(plugin, damageValue));
        }

        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!fireball.isValid() || fireball.isDead() || target.isDead() || !target.getWorld().equals(world)) {
                    cancel();
                    return;
                }

                Vector newDir = target.getEyeLocation().toVector().subtract(fireball.getLocation().toVector()).normalize();
                fireball.setVelocity(newDir.multiply(0.5));

                Location loc = fireball.getLocation();

                world.spawnParticle(Particle.FLAME, loc, 4, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.REDSTONE, loc, 2, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 180, 50), 1.7f));
                world.spawnParticle(Particle.SMOKE_NORMAL, loc, 2, 0.1, 0.1, 0.1, 0.02);

                angle += Math.PI / 6;
                double radius = 0.4;
                for (int i = 0; i < 3; i++) {
                    double x = radius * Math.cos(angle + i * Math.PI / 2);
                    double y = radius * Math.sin(angle + i * Math.PI / 2);
                    Location vortex = loc.clone().add(x, y, -i * 0.1);
                    world.spawnParticle(Particle.CRIT, vortex, 1, 0, 0, 0, 0.01);
                    world.spawnParticle(Particle.LAVA, vortex, 1, 0.05, 0.05, 0.05, 0.01);
                    world.spawnParticle(Particle.SPELL_WITCH, vortex, 1, 0, 0, 0, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}