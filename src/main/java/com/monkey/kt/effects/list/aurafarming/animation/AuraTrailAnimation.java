package com.monkey.kt.effects.list.aurafarming.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.aurafarming.animation.util.AuraBoostApplier;
import com.monkey.kt.effects.list.aurafarming.animation.util.AuraHomingTrail;
import com.monkey.kt.effects.list.aurafarming.animation.util.AuraParticles;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class AuraTrailAnimation {

    private final KT plugin;
    private final Location origin;
    private final Player killer;

    public AuraTrailAnimation(KT plugin, Location origin, Player killer) {
        this.plugin = plugin;
        this.origin = origin;
        this.killer = killer;
    }

    public void start() {
        World world = origin.getWorld();
        if (world == null) return;

        List<Vector> directions = new ArrayList<>();
        directions.add(new Vector(-0.4, 1, -0.4).normalize());
        directions.add(new Vector(-0.6, 1, 0).normalize());
        directions.add(new Vector(-0.4, 1, 0.4).normalize());
        directions.add(new Vector(0, 1, 0).normalize());
        directions.add(new Vector(0.4, 1, 0.4).normalize());
        directions.add(new Vector(0.6, 1, 0).normalize());
        directions.add(new Vector(0.4, 1, -0.4).normalize());

        new BukkitRunnable() {
            int ticks = 0;
            final List<Location> currentPositions = new ArrayList<>();
            final List<Boolean> trailEnded = new ArrayList<>();

            {
                for (int i = 0; i < directions.size(); i++) {
                    currentPositions.add(origin.clone());
                    trailEnded.add(false);
                }
            }

            @Override
            public void run() {
                if (ticks > 200) {
                    cancel();
                    return;
                }

                for (int i = 0; i < currentPositions.size(); i++) {
                    if (trailEnded.get(i)) continue;

                    Location currentLoc = currentPositions.get(i);

                    if (ticks <= 80) {
                        Vector dir = directions.get(i).clone().multiply(0.15);
                        currentPositions.set(i, currentLoc.add(dir));

                    } else if (ticks <= 120) {
                        Vector toKiller = killer.getLocation().clone().add(0, 1.5, 0)
                                .subtract(currentLoc).toVector().normalize().multiply(0.12);
                        currentPositions.set(i, currentLoc.add(toKiller));

                    } else if (ticks > 120 && ticks <= 140) {
                        Location killerLoc = killer.getLocation().clone().add(0, 0.5, 0);
                        int ticksLeft = 140 - ticks + 1;

                        Vector toKiller = killerLoc.toVector().subtract(currentLoc.toVector()).multiply(1.0 / ticksLeft);
                        currentPositions.set(i, currentLoc.add(toKiller));

                        if (ticks == 140) {
                            world.strikeLightningEffect(killerLoc);
                            int finalI = i;
                            new BukkitRunnable() {
                                int explosionTick = 0;
                                @Override
                                public void run() {
                                    if (explosionTick > 10) {
                                        cancel();
                                        return;
                                    }
                                    AuraParticles.spawnExplosionEffect(currentPositions.get(finalI), explosionTick);
                                    explosionTick++;
                                }
                            }.runTaskTimer(plugin, 0L, 2L);

                            if (i == 0) {
                                DamageConfig damageConfig = DamageUtils.getDamageConfig("aurafarming", plugin);
                                if (damageConfig.isEnabled()) {
                                    DamageUtils.applyDamageAround(killer, killerLoc, damageConfig.getRadius(), damageConfig.getValue());
                                }
                                AuraBoostApplier.applyBoosts(plugin, killer);
                                List<Player> nearbyPlayers = killer.getWorld().getPlayers();
                                for (Player p : nearbyPlayers) {
                                    if (p.equals(killer)) continue;
                                    if (p.getLocation().distance(killer.getLocation()) <= 100) {
                                        new AuraHomingTrail(plugin, killer, p).start();
                                    }
                                }
                            }

                            trailEnded.set(i, true);
                        }
                    }

                    AuraParticles.spawnThickWhiteSparkle(currentPositions.get(i));
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
