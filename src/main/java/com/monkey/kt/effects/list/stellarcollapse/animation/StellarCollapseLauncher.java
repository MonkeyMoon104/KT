package com.monkey.kt.effects.list.stellarcollapse.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.stellarcollapse.animation.util.StellarParticles;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class StellarCollapseLauncher {

    public static void launch(KT plugin, Location center) {
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    StellarParticles.spawnCollapseImplosion(plugin, center);
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 2.0f, 0.8f);
                    cancel();
                    return;
                }

                double radius = 1 + (ticks * 0.1);
                StellarParticles.spawnStellarSwirl(world, center, radius, 80);
                if (ticks % 5 == 0) {
                    world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 2.0f, 1.5f - ticks * 0.03f);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
