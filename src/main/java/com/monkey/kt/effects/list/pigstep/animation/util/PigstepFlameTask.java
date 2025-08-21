package com.monkey.kt.effects.list.pigstep.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class PigstepFlameTask implements Runnable {
    private final KT plugin;
    private final Location loc;
    private int ticks = 0;
    private SchedulerWrapper.ScheduledTask task;

    public PigstepFlameTask(KT plugin, Location loc) {
        this.plugin = plugin;
        this.loc = loc;
    }

    public void setTask(SchedulerWrapper.ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        if (ticks++ > 10) {
            if (task != null) {
                task.cancel();
            }
            return;
        }

        World world = loc.getWorld();
        if (world == null) return;

        Location flameLoc = loc.clone().add(0, ticks * 0.5, 0);
        world.spawnParticle(Particle.FLAME, flameLoc, 20, 0.3, 0.3, 0.3, 0);
    }
}