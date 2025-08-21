package com.monkey.kt.effects.list.sniper;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class SniperEffect implements KillEffect {

    private final KT plugin;
    private final Random random = new Random();

    public SniperEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location target) {
        World world = target.getWorld();
        if (world == null) return;

        String soundName = plugin.getConfig().getString("resource_pack.sounds.bow-hit.name");
        if (soundName != null) {
            world.playSound(target, soundName, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        DamageConfig damageConfig = DamageUtils.getDamageConfig("sniper", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, target, damageConfig.getRadius(), damageConfig.getValue());
        }

        final int[] count = {0};
        final int max = 20;
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask sniperTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;
                if (count[0]++ >= max) {
                    taskCompleted[0] = true;
                    return;
                }

                Location sniperPos = getRandomSniperPosition(target);
                Vector direction = target.clone().add(0, 1.3, 0).toVector().subtract(sniperPos.toVector()).normalize();

                Arrow arrow = world.spawnArrow(sniperPos, direction, 3.5f, 0f);
                arrow.setGravity(true);
                arrow.setDamage(0.0);
                arrow.setSilent(true);
                arrow.setCritical(false);
                arrow.setKnockbackStrength(0);
                arrow.setPersistent(false);
                arrow.setInvulnerable(true);
                arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

                world.spawnParticle(Particle.CRIT, sniperPos, 5, 0.1, 0.1, 0.1, 0.1);
                world.playSound(sniperPos, Sound.ENTITY_ARROW_SHOOT, 1.6f, 1.2f + random.nextFloat() * 0.4f);

                final boolean[] arrowTaskCompleted = {false};
                SchedulerWrapper.ScheduledTask arrowRemovalTask = SchedulerWrapper.runTaskAtLocation(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (arrowTaskCompleted[0]) return;
                        arrowTaskCompleted[0] = true;
                        if (!arrow.isDead()) {
                            arrow.remove();
                        }
                    }
                }, sniperPos, 20L);
            }
        }, target, 0L, 2L);
    }

    private Location getRandomSniperPosition(Location target) {
        double radius = 8 + random.nextDouble() * 8;
        double angle = random.nextDouble() * 2 * Math.PI;
        double dx = Math.cos(angle) * radius;
        double dz = Math.sin(angle) * radius;
        double dy = 6 + random.nextDouble() * 4;

        return target.clone().add(dx, dy, dz);
    }
}