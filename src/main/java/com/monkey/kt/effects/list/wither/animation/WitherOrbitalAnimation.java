package com.monkey.kt.effects.list.wither.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.wither.animation.util.WitherParticles;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WitherOrbitalAnimation {

    private final KT plugin;
    private final Location center;
    private final Player killer;

    public WitherOrbitalAnimation(KT plugin, Location center, Player killer) {
        this.plugin = plugin;
        this.center = center;
        this.killer = killer;
    }

    public void start() {
        World world = center.getWorld();
        if (world == null) return;

        Wither wither1 = spawnControlledWither(center.clone().add(5, 0, 0));
        Wither wither2 = spawnControlledWither(center.clone().add(-5, 0, 0));

        new BukkitRunnable() {
            double angle = 0;
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 100) {
                    wither1.remove();
                    wither2.remove();
                    cancel();
                    WitherParticles.spawnFinalImplosion(plugin, center);
                    world.playSound(center, Sound.AMBIENT_CAVE, 3.0f, 0.4f);
                    return;
                }

                angle += Math.PI / 30;

                double radius = 5;
                double x1 = Math.cos(angle) * radius;
                double z1 = Math.sin(angle) * radius;
                double y1 = Math.sin(angle / 2) * 2;

                double x2 = Math.cos(angle + Math.PI) * radius;
                double z2 = Math.sin(angle + Math.PI) * radius;
                double y2 = Math.cos(angle / 2) * 2;

                Location pos1 = center.clone().add(x1, y1, z1);
                Location pos2 = center.clone().add(x2, y2, z2);

                orientLocationTowardsPlayer(pos1, killer);
                orientLocationTowardsPlayer(pos2, killer);

                wither1.teleport(pos1);
                wither2.teleport(pos2);

                if (ticks % 10 == 0) {
                    launchSkull(world, pos1, center);
                    launchSkull(world, pos2, center);
                }

                world.spawnParticle(Particle.SOUL, pos1, 5, 0.2, 0.2, 0.2, 0.01);
                world.spawnParticle(Particle.ASH, pos2, 5, 0.2, 0.2, 0.2, 0.01);
                if (ticks % 20 == 0) {
                    world.strikeLightningEffect(center.clone().add(Math.random()*4-2, 0, Math.random()*4-2));
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void orientLocationTowardsPlayer(Location from, Player player) {
        Location to = player.getEyeLocation();

        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        float pitch = (float) Math.toDegrees(Math.asin(-direction.getY()));

        from.setYaw(yaw);
        from.setPitch(pitch);
    }

    private Wither spawnControlledWither(Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        Wither wither = world.spawn(loc, Wither.class, entity -> {
            entity.setMetadata("kt_bypass_spawn", new FixedMetadataValue(plugin, true));
            entity.setAI(false);
            entity.setCustomNameVisible(false);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.setCollidable(false);
            entity.setHealth(300);
            entity.setCustomName("§7§oSpectral Wither");
        });

        return wither;
    }


    private void launchSkull(World world, Location from, Location to) {
        Vector direction = to.clone().subtract(from).toVector().normalize();

        if (direction.getY() >= 0) return;

        WitherSkull skull = world.spawn(from.clone().add(0, 1, 0), WitherSkull.class);
        skull.setDirection(direction);
        skull.setVelocity(direction.multiply(0.7));
        skull.setInvulnerable(true);
        skull.setYield(0);
        skull.setCharged(true);
        skull.setGravity(true);
        skull.setSilent(true);
        skull.setMetadata("kt_wither_skull", new FixedMetadataValue(plugin, true));

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!skull.isValid() || skull.isDead()) {
                    cancel();
                    return;
                }
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, skull.getLocation(), 2, 0, 0, 0, 0.01);
                world.spawnParticle(Particle.SMOKE_LARGE, skull.getLocation(), 1, 0.05, 0.05, 0.05, 0.01);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
