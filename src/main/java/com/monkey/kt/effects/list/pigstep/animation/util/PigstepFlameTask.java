package com.monkey.kt.effects.list.pigstep.animation.util;

import com.monkey.kt.KT;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class PigstepFlameTask extends BukkitRunnable {
    private final KT plugin;
    private final Location loc;
    private int ticks = 0;

    public PigstepFlameTask(KT plugin, Location loc) {
        this.plugin = plugin;
        this.loc = loc;
    }

    @Override
    public void run() {
        if (ticks++ > 10) {
            cancel();
            return;
        }
        World world = loc.getWorld();
        if (world == null) return;

        Location flameLoc = loc.clone().add(0, ticks * 0.5, 0);
        world.spawnParticle(Particle.FLAME, flameLoc, 20, 0.3, 0.3, 0.3, 0);
    }
}
