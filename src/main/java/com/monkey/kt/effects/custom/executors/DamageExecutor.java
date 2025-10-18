package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageExecutor {

    private final KT plugin;

    public DamageExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(Player killer, Location location, double damage, double radius) {
        if (location.getWorld() == null) {
            return;
        }

        for (LivingEntity entity : location.getWorld().getLivingEntities()) {
            if (entity.equals(killer)) {
                continue;
            }

            if (entity.getLocation().distance(location) <= radius) {
                entity.damage(damage, killer);
            }
        }
    }
}