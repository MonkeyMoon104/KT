package com.monkey.kt.effects.list.firephoenix.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.entity.EntityDataUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
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

        EntityDataUtils.setBoolean(fireball, plugin, "kt_phoenix_firecharge", true);

        if (shouldDamage) {
            EntityDataUtils.setBoolean(fireball, plugin, "kt_phoenix_damage_enabled", true);
            EntityDataUtils.setDouble(fireball, plugin, "kt_phoenix_damage_value", damageValue);
        }

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimer(plugin, new Runnable() {
            double angle = 0;
            int lifetimeTicks = 0;

            @Override
            public void run() {
                if (taskCompleted[0]) return;

                if (!fireball.isValid() || fireball.isDead() || target.isDead() || !target.getWorld().equals(world) || lifetimeTicks >= 30) {
                    if (fireball.isValid() && !fireball.isDead()) {
                        fireball.remove();
                    }
                    taskCompleted[0] = true;
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                Vector newDir = target.getEyeLocation().toVector().subtract(fireball.getLocation().toVector()).normalize();
                fireball.setVelocity(newDir.multiply(0.42));

                Location loc = fireball.getLocation();

                world.spawnParticle(Particle.FLAME, loc, 2, 0.08, 0.08, 0.08, 0.02);
                world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 180, 50), 1.1f));
                world.spawnParticle(Particle.SMOKE, loc, 1, 0.05, 0.05, 0.05, 0.01);

                angle += Math.PI / 6;
                double radius = 0.22;
                for (int i = 0; i < 2; i++) {
                    double x = radius * Math.cos(angle + i * Math.PI / 2);
                    double y = radius * Math.sin(angle + i * Math.PI / 2);
                    Location vortex = loc.clone().add(x, y, -i * 0.1);
                    world.spawnParticle(Particle.ENCHANTED_HIT, vortex, 1, 0, 0, 0, 0.01);
                    world.spawnParticle(Particle.LAVA, vortex, 1, 0.02, 0.02, 0.02, 0.01);
                }

                lifetimeTicks++;
            }
        }, 0L, 2L);
    }
}
