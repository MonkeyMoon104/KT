package com.monkey.kt.effects.list.pigstep.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.pigstep.animation.PigstepFollowTask;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.metadata.FixedMetadataValue;

public class PigstepPigSpawner implements Runnable {
    private final KT plugin;
    private final Player killer;
    private final Location loc;

    public PigstepPigSpawner(KT plugin, Player killer, Location loc) {
        this.plugin = plugin;
        this.killer = killer;
        this.loc = loc;
    }

    @Override
    public void run() {
        if (loc.getWorld() == null) return;

        Pig pig = (Pig) loc.getWorld().spawn(loc, Pig.class, entity -> {
            entity.setMetadata("kt_pigstep", new FixedMetadataValue(plugin, true));
            entity.setMetadata("kt_bypass_spawn", new FixedMetadataValue(plugin, true));
            entity.setAI(true);
            entity.setInvulnerable(true);
            entity.setCollidable(false);
            entity.setSilent(true);
            entity.setFallDistance(0);
            entity.setVelocity(new Vector(0, 1.2, 0));
        });

        killer.stopSound(org.bukkit.Sound.MUSIC_DISC_PIGSTEP, org.bukkit.SoundCategory.RECORDS);
        killer.playSound(
                killer.getLocation(),
                Sound.MUSIC_DISC_PIGSTEP,
                SoundCategory.RECORDS,
                1.2f,
                1.0f
        );

        SchedulerWrapper.runTaskLaterAsynchronously(plugin, () -> {
            if (pig.isValid() && !pig.isDead()) {
                startFollowTask(pig, killer);
            }
        }, 5L);

        DamageConfig damageConfig = DamageUtils.getDamageConfig("pigstep", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, loc, damageConfig.getRadius(), damageConfig.getValue());
        }
    }

    private void startFollowTask(Pig pig, Player killer) {
        final int[] timer = {0};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAsynchronously(plugin, () -> {
            timer[0]++;

            if (timer[0] > 100 || !pig.isValid() || pig.isDead()) {
                if (pig.isValid()) {
                    pig.getScheduler().run(plugin, (t) -> {
                        try {
                            pig.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, pig.getLocation(), 20, 0.4, 0.4, 0.4, 0.01);
                            killer.stopSound(org.bukkit.Sound.MUSIC_DISC_PIGSTEP, org.bukkit.SoundCategory.RECORDS);
                            pig.remove();
                        } catch (Exception e) {
                        }
                    }, null);
                }
                return;
            }

            if (timer[0] > 5 && killer.isOnline()) {
                pig.getScheduler().run(plugin, (t) -> {
                    try {
                        double distance = pig.getLocation().distance(killer.getLocation());
                        if (distance > 2 && distance < 50) {
                            pig.getPathfinder().moveTo(killer, 1.2);

                            org.bukkit.Color pinkColor = org.bukkit.Color.fromRGB(255, 105, 180);
                            org.bukkit.Particle.DustOptions pinkDust = new org.bukkit.Particle.DustOptions(pinkColor, 1.5F);
                            pig.getWorld().spawnParticle(
                                    org.bukkit.Particle.DUST,
                                    pig.getLocation().add(0, 0.3, 0),
                                    10, 0.2, 0.05, 0.2, 0,
                                    pinkDust
                            );
                        }
                    } catch (Exception e) {
                    }
                }, null);
            }
        }, 10L, 2L);
    }
}