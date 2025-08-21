package com.monkey.kt.effects.list.warden;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WardenEffect implements KillEffect {
    private final KT plugin;

    public WardenEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Location particleLoc = loc.clone().add(0, 3, 0);
        Location shriekLoc = loc.clone();
        Location soundLoc = loc.clone().add(0, 1, 0);

        killer.playSound(soundLoc, Sound.ENTITY_WARDEN_EMERGE, 2.0f, 1.0f);

        DamageConfig damageConfig = DamageUtils.getDamageConfig("warden", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, loc, damageConfig.getRadius(), damageConfig.getValue());
        }

        final double radius = 1.5;
        final int points = 36;

        int[] finishedCount = {0};
        List<Location> impactLocations = new ArrayList<>();

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle);
            double z = Math.sin(angle);

            Location start = loc.clone().add(x * radius, 0.5, z * radius);
            Vector velocity = new Vector(x * 0.05, 0.10, z * 0.05);

            Location current = start.clone();
            Vector vel = velocity.clone();
            final int[] age = {0};
            final boolean[] taskCompleted = {false};

            SchedulerWrapper.ScheduledTask projectileTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
                @Override
                public void run() {
                    if (taskCompleted[0]) return;
                    if (age[0] > 40 || current.getY() <= loc.getY()) {
                        taskCompleted[0] = true;
                        finishedCount[0]++;
                        impactLocations.add(current.clone());

                        if (finishedCount[0] >= points) {
                            Location center = getAverageLocation(impactLocations);
                            spawnStaticSplashRing(center, world);
                            spawnExpandingRing(center, world, killer);
                        }
                        return;
                    }

                    world.spawnParticle(
                            Particle.SCULK_SOUL,
                            current,
                            0,
                            0, 0, 0,
                            0
                    );

                    current.add(vel);
                    vel.setY(vel.getY() - 0.015);
                    age[0]++;
                }
            }, start, i % 4, 1L);
        }

        final int[] ticks = {0};
        final boolean[] shriekTaskCompleted = {false};

        SchedulerWrapper.ScheduledTask shriekTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (shriekTaskCompleted[0]) return;
                if (ticks[0] > 85) {
                    shriekTaskCompleted[0] = true;
                    return;
                }

                if (ticks[0] % 2 == 0) {
                    world.spawnParticle(Particle.SHRIEK, shriekLoc, 150, 0, 0, 0, 1.5, 0);
                }

                if (ticks[0] >= 15 && (ticks[0] - 15) % 8 == 0) {
                    world.spawnParticle(Particle.SONIC_BOOM, particleLoc, 1, 0, 0, 0.5, 0);
                }

                ticks[0]++;
            }
        }, shriekLoc, 0L, 1L);
    }

    private Location getAverageLocation(List<Location> locations) {
        if (locations.isEmpty()) return null;

        double x = 0, y = 0, z = 0;
        World world = locations.get(0).getWorld();

        for (Location loc : locations) {
            x += loc.getX();
            y += loc.getY();
            z += loc.getZ();
        }

        return new Location(world, x / locations.size(), y / locations.size(), z / locations.size());
    }

    private void spawnStaticSplashRing(Location center, World world) {
        int ringPoints = 48;
        double splashRadius = 2.2;

        for (int j = 0; j < ringPoints; j++) {
            double angle = 2 * Math.PI * j / ringPoints;
            double rx = Math.cos(angle) * splashRadius;
            double rz = Math.sin(angle) * splashRadius;

            Location ringLoc = center.clone().add(rx, 0.2, rz);

            world.spawnParticle(
                    Particle.FLAME,
                    ringLoc,
                    1,
                    0, 0.01, 0,
                    0
            );
        }
    }

    private void spawnExpandingRing(Location center, World world, Player killer) {
        int ringPoints = 64;
        double splashRadius = 2.2;
        double maxRadius = 6.0;
        int steps = 35;
        double radiusIncrement = (maxRadius - splashRadius) / steps;

        List<Vector> directions = Arrays.asList(
                new Vector(-1, 0, -1).normalize(),
                new Vector(1, 0, -1).normalize(),
                new Vector(-1, 0, 1).normalize(),
                new Vector(1, 0, 1).normalize()
        );

        List<Warden> wardens = new ArrayList<>();

        for (Vector dir : directions) {
            Location spawnLoc = center.clone();
            Warden warden = (Warden) world.spawn(spawnLoc, Warden.class, entity -> {
                entity.setMetadata("kt_bypass_spawn", new FixedMetadataValue(plugin, true));

                entity.setAI(false);
                entity.setSilent(true);
                entity.setInvulnerable(true);
                entity.setAware(false);
                entity.setPersistent(false);
                entity.setCanPickupItems(false);
                entity.setRemoveWhenFarAway(true);
                entity.setCollidable(false);
                entity.setGravity(false);

                entity.setMetadata("kt_direction", new FixedMetadataValue(plugin, dir.clone()));
            });

            wardens.add(warden);
        }

        final int[] currentStep = {0};
        final boolean[] ringTaskCompleted = {false};

        SchedulerWrapper.ScheduledTask ringTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (ringTaskCompleted[0]) return;
                if (currentStep[0] > steps) {
                    ringTaskCompleted[0] = true;
                    for (Warden warden : wardens) {
                        if (warden.isValid()) {
                            warden.remove();
                        }
                    }
                    return;
                }

                double currentRadius = splashRadius + currentStep[0] * radiusIncrement;

                for (int j = 0; j < ringPoints; j++) {
                    double angle = 2 * Math.PI * j / ringPoints;
                    double rx = Math.cos(angle) * currentRadius;
                    double rz = Math.sin(angle) * currentRadius;

                    Location ringLoc = center.clone().add(rx, 0.2, rz);

                    world.spawnParticle(
                            Particle.SOUL,
                            ringLoc,
                            1,
                            0, 0.01, 0,
                            0
                    );
                }

                for (Warden warden : wardens) {
                    if (!warden.isValid()) continue;

                    Vector dir = (Vector) warden.getMetadata("kt_direction").get(0).value();

                    Location newLoc = center.clone().add(dir.clone().multiply(currentRadius));
                    newLoc.setY(center.getY());

                    orientLocationTowardsPlayer(newLoc, killer);

                    warden.teleportAsync(newLoc);

                    drawParticleLine(world, newLoc, center.clone().add(0, 3, 0), Particle.ELECTRIC_SPARK, 0.15);
                }

                currentStep[0]++;
            }
        }, center, 0L, 2L);
    }

    private void orientLocationTowardsPlayer(Location from, Player player) {
        Location to = player.getEyeLocation();

        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        float pitch = (float) Math.toDegrees(Math.asin(-direction.getY()));

        from.setYaw(yaw);
        from.setPitch(pitch);
    }

    private void drawParticleLine(World world, Location start, Location end, Particle particle, double step) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        for (double i = 0; i <= length; i += step) {
            Location point = start.clone().add(direction.clone().multiply(i));

            double offset = Math.sin(i * 10 + System.currentTimeMillis() * 0.01) * 0.05;
            point.add(direction.clone().rotateAroundY(Math.PI / 2).multiply(offset));

            world.spawnParticle(
                    particle,
                    point,
                    1,
                    0, 0, 0,
                    0.01
            );
        }
    }
}