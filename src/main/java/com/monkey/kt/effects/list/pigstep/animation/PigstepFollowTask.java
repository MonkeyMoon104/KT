package com.monkey.kt.effects.list.pigstep.animation;

import com.monkey.kt.KT;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import com.destroystokyo.paper.entity.ai.MobGoals;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;

public class PigstepFollowTask implements Runnable {
    private final KT plugin;
    private final Pig pig;
    private final Player killer;

    private boolean goalSet = false;
    private int timer = 0;
    private SchedulerWrapper.ScheduledTask task;
    private boolean useAI = true;

    public PigstepFollowTask(KT plugin, Pig pig, Player killer) {
        this.plugin = plugin;
        this.pig = pig;
        this.killer = killer;
    }

    public void setTask(SchedulerWrapper.ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        if (!pig.isValid() || pig.isDead()) {
            if (task != null) {
                task.cancel();
            }
            return;
        }

        if (timer++ > 100) {
            try {
                pig.getWorld().spawnParticle(Particle.LARGE_SMOKE, pig.getLocation(), 20, 0.4, 0.4, 0.4, 0.01);
                killer.stopSound(org.bukkit.Sound.MUSIC_DISC_PIGSTEP, org.bukkit.SoundCategory.RECORDS);
                pig.remove();
            } catch (Exception e) {
            }

            if (task != null) {
                task.cancel();
            }
            return;
        }

        if (!goalSet && pig.isOnGround()) {
            goalSet = true;

            try {
                if (SchedulerWrapper.isFolia()) {
                    MobGoals goals = org.bukkit.Bukkit.getMobGoals();
                    goals.removeAllGoals(pig);
                    goals.addGoal(pig, 1, new PigstepFollowPlayerGoal(plugin, pig, killer, 1.2));
                } else {
                    useAI = false;
                }
            } catch (Exception e) {
                useAI = false;
            }
        }

        if (!useAI && goalSet && killer.isOnline()) {
            try {
                double distance = pig.getLocation().distance(killer.getLocation());
                if (distance > 2 && distance < 50) {
                    pig.getPathfinder().moveTo(killer, 1.2);

                    Particle.DustOptions pinkDust = new Particle.DustOptions(Color.fromRGB(255, 105, 180), 1.5F);
                    pig.getWorld().spawnParticle(
                            Particle.DUST,
                            pig.getLocation().add(0, 0.3, 0),
                            10, 0.2, 0.05, 0.2, 0,
                            pinkDust
                    );
                }
            } catch (Exception e) {
            }
        }
    }
}