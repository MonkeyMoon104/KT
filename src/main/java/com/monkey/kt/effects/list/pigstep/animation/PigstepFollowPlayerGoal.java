package com.monkey.kt.effects.list.pigstep.animation;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class PigstepFollowPlayerGoal implements Goal<Pig> {
    private final Pig pig;
    private final Player target;
    private final double speed;
    private final Plugin plugin;

    private final DustOptions pinkDust = new DustOptions(Color.fromRGB(255, 105, 180), 1.5F);

    public PigstepFollowPlayerGoal(Plugin plugin, Pig pig, Player target, double speed) {
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
