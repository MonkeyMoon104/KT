package com.monkey.kt.effects.list.pigstep.animation;

import com.monkey.kt.KT;
import com.destroystokyo.paper.entity.ai.MobGoals;
import org.bukkit.Particle;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PigstepFollowTask extends BukkitRunnable {
    private final KT plugin;
    private final Pig pig;
    private final Player killer;

    private boolean goalSet = false;
    private int timer = 0;

    public PigstepFollowTask(KT plugin, Pig pig, Player killer) {
        this.plugin = plugin;
        this.pig = pig;
        this.killer = killer;
    }

    @Override
    public void run() {
        if (timer++ > 100 || !pig.isValid()) {
            pig.getWorld().spawnParticle(Particle.LARGE_SMOKE, pig.getLocation(), 20, 0.4, 0.4, 0.4, 0.01);
            killer.stopSound(org.bukkit.Sound.MUSIC_DISC_PIGSTEP, org.bukkit.SoundCategory.RECORDS);
            pig.remove();
            cancel();
            return;
        }

        if (!goalSet && pig.isOnGround()) {
            goalSet = true;

            MobGoals goals = org.bukkit.Bukkit.getMobGoals();
            goals.removeAllGoals(pig);
            goals.addGoal(pig, 1, new PigstepFollowPlayerGoal(plugin, pig, killer, 1.2));
        }
    }
}
