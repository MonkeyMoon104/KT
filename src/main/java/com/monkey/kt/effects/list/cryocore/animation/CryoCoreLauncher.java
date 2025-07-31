package com.monkey.kt.effects.list.cryocore.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.CryoCoreParticles;
import com.monkey.kt.effects.list.cryocore.animation.util.CryoCoreStructures;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class CryoCoreLauncher {

    public static void launch(KT plugin, Location center) {
        World world = center.getWorld();
        if (world == null) return;

        CryoCoreStructures.BlockStateHolder stateHolder = new CryoCoreStructures.BlockStateHolder();
        int spikeRadius = 16;

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick <= 30) {
                    CryoCoreParticles.spawnIceExplosion(world, center, tick, spikeRadius - 1);
                    if (tick % 3 == 0) {
                        CryoCoreStructures.randomlyPlaceSnowBlocks(center, 15, stateHolder, tick / 3);
                    }

                } else if (tick == 31) {
                    CryoCoreParticles.spawnInclinedBurst(plugin, center, spikeRadius - 1);
                    world.playSound(center, Sound.BLOCK_GLASS_BREAK, 2f, 0.8f);

                } else if (tick == 45) {
                    CryoCoreStructures.spawnLargeIceSpikesBorder(plugin, center, spikeRadius, stateHolder);
                    world.playSound(center, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.8f, 1f);

                } else if (tick == 110) {
                    CryoCoreParticles.spawnFinalCrystalFade(plugin, world, center,spikeRadius - 1);
                } else if (tick == 120) {
                    CryoCoreStructures.restoreGround(stateHolder);
                    world.playSound(center, Sound.BLOCK_SNOW_BREAK, 1.6f, 1.0f);
                    cancel();
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
