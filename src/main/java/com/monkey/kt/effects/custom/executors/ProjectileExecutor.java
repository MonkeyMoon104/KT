package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.List;

public class ProjectileExecutor {

    private final KT plugin;

    public ProjectileExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(List<CustomEffectConfig.ProjectileData> projectiles, Player killer, Location location) {
        if (projectiles == null || projectiles.isEmpty()) {
            return;
        }

        for (CustomEffectConfig.ProjectileData projectileData : projectiles) {
            if (projectileData.getDelay() > 0) {
                SchedulerWrapper.runTaskLater(plugin, () -> {
                    launchProjectiles(projectileData, killer, location);
                }, projectileData.getDelay());
            } else {
                launchProjectiles(projectileData, killer, location);
            }
        }
    }

    private void launchProjectiles(CustomEffectConfig.ProjectileData data, Player killer, Location location) {
        int count = data.getCount();
        int spread = data.getSpread();

        for (int i = 0; i < count; i++) {
            Vector direction = getSpreadDirection(killer.getLocation().getDirection(), spread, count, i);
            direction.multiply(data.getSpeed());

            Projectile projectile = launchProjectile(data.getType(), killer, location, direction);

            if (projectile != null) {
                projectile.setGravity(data.hasGravity());
                projectile.setMetadata("kt_custom_projectile", new FixedMetadataValue(plugin, true));
                projectile.setMetadata("kt_custom_damage", new FixedMetadataValue(plugin, data.getDamage()));
                projectile.setMetadata("kt_custom_remove", new FixedMetadataValue(plugin, data.shouldRemoveOnHit()));

                if (!data.getTrailParticle().isEmpty()) {
                    addTrailEffect(projectile, data.getTrailParticle());
                }
            }
        }
    }

    private Projectile launchProjectile(String type, Player killer, Location location, Vector direction) {
        Location spawnLoc = location.clone().add(0, 1, 0);

        try {
            switch (type.toUpperCase()) {
                case "ARROW":
                    Arrow arrow = killer.launchProjectile(Arrow.class);
                    arrow.setVelocity(direction);
                    arrow.teleport(spawnLoc);
                    return arrow;

                case "FIREBALL":
                    Fireball fireball = killer.launchProjectile(Fireball.class);
                    fireball.setDirection(direction);
                    fireball.teleport(spawnLoc);
                    fireball.setYield(0);
                    return fireball;

                case "SMALL_FIREBALL":
                    SmallFireball smallFireball = killer.launchProjectile(SmallFireball.class);
                    smallFireball.setDirection(direction);
                    smallFireball.teleport(spawnLoc);
                    return smallFireball;

                case "SNOWBALL":
                    Snowball snowball = killer.launchProjectile(Snowball.class);
                    snowball.setVelocity(direction);
                    snowball.teleport(spawnLoc);
                    return snowball;

                case "EGG":
                    Egg egg = killer.launchProjectile(Egg.class);
                    egg.setVelocity(direction);
                    egg.teleport(spawnLoc);
                    return egg;

                case "ENDER_PEARL":
                    EnderPearl pearl = killer.launchProjectile(EnderPearl.class);
                    pearl.setVelocity(direction);
                    pearl.teleport(spawnLoc);
                    return pearl;

                case "TRIDENT":
                    Trident trident = killer.launchProjectile(Trident.class);
                    trident.setVelocity(direction);
                    trident.teleport(spawnLoc);
                    return trident;

                default:
                    plugin.getLogger().warning("Unknown projectile type: " + type);
                    return null;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to launch projectile " + type + ": " + e.getMessage());
            return null;
        }
    }

    private Vector getSpreadDirection(Vector baseDirection, int spreadDegrees, int totalProjectiles, int index) {
        if (totalProjectiles == 1 || spreadDegrees == 0) {
            return baseDirection.clone();
        }

        double spreadRadians = Math.toRadians(spreadDegrees);
        double angleStep = spreadRadians / (totalProjectiles - 1);
        double angle = -spreadRadians / 2 + (angleStep * index);

        Vector direction = baseDirection.clone();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double x = direction.getX() * cos - direction.getZ() * sin;
        double z = direction.getX() * sin + direction.getZ() * cos;

        return new Vector(x, direction.getY(), z).normalize();
    }

    private void addTrailEffect(Projectile projectile, String particleName) {
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());

            SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAtEntity(plugin, new Runnable() {
                @Override
                public void run() {
                    if (!projectile.isValid() || projectile.isDead()) {
                        SchedulerWrapper.safeCancelTask(this);
                        return;
                    }

                    Location loc = projectile.getLocation();
                    if (loc.getWorld() != null) {
                        loc.getWorld().spawnParticle(particle, loc, 2, 0.1, 0.1, 0.1, 0.01);
                    }
                }
            }, projectile, 0L, 1L);

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid trail particle: " + particleName);
        }
    }
}