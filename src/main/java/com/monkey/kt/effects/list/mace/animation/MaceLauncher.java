package com.monkey.kt.effects.list.mace.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.mace.animation.util.MaceParticles;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MaceLauncher {

    public static void launch(KT plugin, Player killer, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 25;
            final double maxRadius = 4;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    MaceParticles.spawnGoldenShockwave(loc);
                    world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
                    new MaceOrbitalAnimation(plugin, loc.clone(), killer).start();
                    cancel();
                    return;
                }

                double radius = (maxRadius * ticks) / maxTicks;
                MaceParticles.spawnChargingSphere(world, loc, radius, 120);
                world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.5f);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
