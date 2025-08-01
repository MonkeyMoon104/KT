package com.monkey.kt.utils.damage;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DamageUtils {

    public static DamageConfig getDamageConfig(String effectName, Plugin plugin) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("effects." + effectName.toLowerCase() + ".damage");
        if (section == null) return new DamageConfig(false, 0, 0);
        boolean enabled = section.getBoolean("enabled", false);
        double value = section.getDouble("value", 0);
        double radius = section.getDouble("radius", 0);
        return new DamageConfig(enabled, value, radius);
    }

    public static void applyDamageAround(Player killer, Location center, double radius, double damage) {
        for (LivingEntity entity : center.getWorld().getLivingEntities()) {
            if (entity.equals(killer)) continue;
            if (entity.getLocation().distance(center) <= radius) {
                entity.damage(damage, killer);
            }
        }
    }
}

