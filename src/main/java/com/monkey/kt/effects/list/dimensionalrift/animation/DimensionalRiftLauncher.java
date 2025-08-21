package com.monkey.kt.effects.list.dimensionalrift.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.dimensionalrift.animation.util.RiftParticles;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DimensionalRiftLauncher {

    public static void launch(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimer(plugin, new Runnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick == 0) {
                    world.playSound(center, Sound.ENTITY_ENDERMAN_STARE, 5f, 1f);
                }

                if (tick < 60) {
                    RiftParticles.spawnHugeOpeningRing(world, center, tick);
                    if (tick % 8 == 0) {
                        world.playSound(center, Sound.BLOCK_PORTAL_AMBIENT, 2f + tick * 0.05f, 0.9f + (tick * 0.02f));
                    }
                } else if (tick < 120) {
                    RiftParticles.spawnGlitchVortex(world, center, tick - 60);
                    RiftParticles.spawnGlitchRingMovement(world, center, tick - 60);
                    if (tick % 7 == 0) {
                        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 3f + (tick - 60) * 0.05f, 0.6f + (tick - 60) * 0.01f);
                    }
                } else if (tick < 160) {
                    RiftParticles.spawnEnergyBeams(world, center, tick - 120);

                    float progress = (tick - 120) / 40f;
                    float pitch = 0.8f + progress * 2.0f;
                    float volume = 2f + progress * 5f;

                    if (tick == 120) {
                        world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, volume, pitch);
                    } else if (tick % 5 == 0) {
                        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, volume * 0.7f, pitch * 0.9f);
                        world.playSound(center, Sound.BLOCK_ANVIL_PLACE, volume * 0.5f, pitch * 1.3f);
                    }
                } else if (tick < 190) {

                    if (tick == 150) {
                        DamageConfig damageConfig = DamageUtils.getDamageConfig("dimensionalrift", plugin);

                        if (damageConfig.isEnabled()) {
                            DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
                        }
                    }

                    if (tick == 160) {
                        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 7.5f, 4.5f);
                    }
                    RiftParticles.spawnClosingSuction(world, center, tick - 160);
                    if (tick % 7 == 0) {
                        world.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 2.5f, 0.5f);
                    }
                } else {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }
                tick++;
            }
        }, 0L, 2L);
    }
}
