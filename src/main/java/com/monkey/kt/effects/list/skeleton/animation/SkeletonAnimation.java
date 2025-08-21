package com.monkey.kt.effects.list.skeleton.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.util.EffectUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.metadata.FixedMetadataValue;

public class SkeletonAnimation {

    private final KT plugin;
    private final Location center;
    private final Player deadPlayer;

    public SkeletonAnimation(KT plugin, Location center, Player deadPlayer) {
        this.plugin = plugin;
        this.center = center;
        this.deadPlayer = deadPlayer;
    }

    public void start() {
        World world = center.getWorld();
        if (world == null) return;

        world.strikeLightningEffect(center);
        world.spawnParticle(Particle.CLOUD, center, 80, 1.5, 1.5, 1.5, 0.05);
        world.playSound(center, Sound.BLOCK_PORTAL_TRAVEL, 2f, 0.6f);

        Skeleton skeleton = world.spawn(center.clone().add(0, 0.1, 0), Skeleton.class, sk -> {
            sk.setMetadata("kt_bypass_spawn", new FixedMetadataValue(plugin, true));
            sk.setAI(false);
            sk.setInvulnerable(true);
            sk.setSilent(true);
            sk.setCollidable(false);
            sk.setCustomName(deadPlayer.getName());
            sk.setCustomNameVisible(true);
            sk.setGravity(false);

            EntityEquipment eq = sk.getEquipment();
            if (eq != null && deadPlayer != null) {
                eq.setHelmet(deadPlayer.getInventory().getHelmet());
                eq.setChestplate(deadPlayer.getInventory().getChestplate());
                eq.setLeggings(deadPlayer.getInventory().getLeggings());
                eq.setBoots(deadPlayer.getInventory().getBoots());
                eq.setItemInMainHand(deadPlayer.getInventory().getItemInMainHand());
                eq.setItemInOffHand(deadPlayer.getInventory().getItemInOffHand());

                eq.setHelmetDropChance(0f);
                eq.setChestplateDropChance(0f);
                eq.setLeggingsDropChance(0f);
                eq.setBootsDropChance(0f);
                eq.setItemInMainHandDropChance(0f);
                eq.setItemInOffHandDropChance(0f);
            }
        });

        final int[] ticks = {0};
        Location loc = skeleton.getLocation();
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask skeletonTask = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (taskCompleted[0]) return;

                if (!skeleton.isValid()) {
                    taskCompleted[0] = true;
                    return;
                }

                if (ticks[0] > 40) {
                    taskCompleted[0] = true;
                    skeleton.remove();
                    world.spawnParticle(Particle.DUST, loc, 40, 0.5, 0.5, 0.5, 0.02, new Particle.DustOptions(Color.fromRGB(255, 255, 255), 1.3f));
                    world.playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, 1f, 0.8f);
                    return;
                }

                loc.subtract(0, 0.05, 0);
                skeleton.teleportAsync(loc);

                EffectUtils.playRepeatingParticle(plugin, loc, Particle.CLOUD, 10, 1.5, 1.5, 1.5, 0.02, 4L, 10);

                ticks[0]++;
            }
        }, center, 0L, 2L);
    }
}