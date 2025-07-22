package com.monkey.kt.effects.list.wither.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.wither.animation.util.WitherParticles;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WitherLauncher {

    public static void launch(KT plugin, Player killer, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 30;
            final double maxRadius = 5;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    WitherParticles.spawnWitherExplosion(loc);
                    world.playSound(loc, org.bukkit.Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);

                    new WitherOrbitalAnimation(plugin, loc.clone(), killer).start();

                    cancel();
                    return;
                }

                double radius = (maxRadius * ticks) / maxTicks;
                WitherParticles.spawnDarkSphere(world, loc, radius, 150);
                world.playSound(loc, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.4f);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
