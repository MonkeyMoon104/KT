package com.monkey.kt.effects.list;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.destroystokyo.paper.entity.ai.MobGoals;
import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle.DustOptions;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class PigstepEffect implements KillEffect {
    private final KT plugin;

    public PigstepEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 10) {
                    cancel();
                    return;
                }
                Location flameLoc = loc.clone().add(0, ticks * 0.5, 0);
                world.spawnParticle(Particle.FLAME, flameLoc, 20, 0.3, 0.3, 0.3, 0);
            }
        }.runTaskTimer(plugin, 0L, 2L);

        new BukkitRunnable() {
            @Override
            public void run() {
                Pig pig = (Pig) world.spawnEntity(loc, EntityType.PIG);
                pig.setMetadata("kt_pigstep", new FixedMetadataValue(plugin, true));
                pig.setAI(true);
                pig.setInvulnerable(true);
                pig.setCollidable(false);
                pig.setSilent(true);
                pig.setFallDistance(0);
                pig.setVelocity(new Vector(0, 1.2, 0));

                killer.playSound(
                        killer.getLocation(),
                        Sound.MUSIC_DISC_PIGSTEP,
                        SoundCategory.RECORDS,
                        1.2f,
                        1.0f
                );

                new BukkitRunnable() {
                    int timer = 0;
                    boolean goalSet = false;

                    @Override
                    public void run() {
                        if (timer++ > 100 || !pig.isValid()) {
                            pig.getWorld().spawnParticle(Particle.SMOKE_NORMAL, pig.getLocation(), 20, 0.4, 0.4, 0.4, 0.01);

                            killer.stopSound(Sound.MUSIC_DISC_PIGSTEP, SoundCategory.RECORDS);

                            pig.remove();
                            cancel();
                            return;
                        }

                        if (!goalSet && pig.isOnGround()) {
                            goalSet = true;

                            MobGoals goals = Bukkit.getMobGoals();
                            goals.removeAllGoals(pig);
                            goals.addGoal(pig, 1, new FollowPlayerGoal(plugin, pig, killer, 1.2));
                        }
                    }
                }.runTaskTimer(plugin, 10L, 2L);
            }
        }.runTaskLater(plugin, 25L);
    }

    public static class FollowPlayerGoal implements Goal<Pig> {
        private final Pig pig;
        private final Player target;
        private final double speed;
        private final Plugin plugin;

        private final DustOptions pinkDust = new DustOptions(Color.fromRGB(255, 105, 180), 1.5F);

        public FollowPlayerGoal(Plugin plugin, Pig pig, Player target, double speed) {
            this.plugin = plugin;
            this.pig = pig;
            this.target = target;
            this.speed = speed;
        }

        @Override
        public boolean shouldActivate() {
            return target.isOnline() && pig.getLocation().distanceSquared(target.getLocation()) > 2;
        }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public void tick() {
            if (!target.isOnline()) return;

            pig.getPathfinder().moveTo(target, speed);
            pig.lookAt(target.getEyeLocation());

            pig.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    pig.getLocation().add(0, 0.3, 0),
                    10, 0.2, 0.05, 0.2, 0,
                    pinkDust
            );
        }

        @Override
        public @NotNull EnumSet<GoalType> getTypes() {
            return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
        }

        @Override
        public @NotNull GoalKey<Pig> getKey() {
            return GoalKey.of(Pig.class, new NamespacedKey(plugin, "follow_player"));
        }
    }
}
