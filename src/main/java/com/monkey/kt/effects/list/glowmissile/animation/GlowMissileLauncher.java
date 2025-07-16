package com.monkey.kt.effects.list.glowmissile.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.glowmissile.animation.util.GlowMissileBlocks;
import com.monkey.kt.effects.list.glowmissile.animation.util.GlowMissileParticles;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GlowMissileLauncher {

    public static void launch(KT plugin, Location startLoc) {
        World world = startLoc.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int height = 0;
            final int maxHeight = 15;
            List<Location> missileBlocks = new ArrayList<>();

            @Override
            public void run() {
                if (height >= maxHeight) {
                    GlowMissileBlocks.clearBlocks(missileBlocks);
                    Location explosionCenter = startLoc.clone().add(0, maxHeight + 1, 0);
                    GlowMissileExplosion.start(plugin, explosionCenter);
                    world.playSound(explosionCenter, org.bukkit.Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 1.0f);
                    cancel();
                    return;
                }

                GlowMissileBlocks.clearBlocks(missileBlocks);
                missileBlocks.clear();

                Location base = startLoc.clone().add(0, height, 0);
                Location top = base.clone().add(0, 1, 0);

                GlowMissileBlocks.placeMissileBlocks(base, top, missileBlocks);
                GlowMissileParticles.spawnEngineParticles(world, top);

                height++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
