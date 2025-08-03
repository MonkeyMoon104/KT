package com.monkey.kt.effects.list.aurafarming.animation.util;

import com.monkey.kt.KT;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AuraBoostApplier {

    public static void applyBoosts(KT plugin, Player killer) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("effects.aurafarming.aura-settings.boost-perks");
        if (section == null) return;

        ConfigurationSection damageBoost = section.getConfigurationSection("damage");
        if (damageBoost != null) {
            int amplifier = damageBoost.getInt("amplifier", 1);
            int duration = damageBoost.getInt("duration", 20);

            plugin.getAuraBoostManager().applyDamageBoost(killer, amplifier, duration);

            int currentAmplifier = plugin.getAuraBoostManager().getDamageAmplifier(killer);

            String message = section.getString("damage-message", "&b☄ You received a damage boost for {duration}s (Amplifier: {amplifier})!");
            message = message.replace("{duration}", String.valueOf(duration));
            message = message.replace("{amplifier}", String.valueOf(currentAmplifier));
            killer.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        ConfigurationSection potionBoost = section.getConfigurationSection("potion");
        if (potionBoost != null) {
            String type = potionBoost.getString("type", "REGENERATION");
            int amplifier = potionBoost.getInt("amplifier", 1);
            int duration = potionBoost.getInt("duration", 10);

            PotionEffectType potionType = getPotionEffectTypeByName(type);
            if (potionType != null) {
                killer.addPotionEffect(new PotionEffect(potionType, duration * 20, amplifier - 1, true, true));

                String message = section.getString("potion-message", "&d✨ You received {potion} {amplifier} for {duration}s!");
                message = message
                        .replace("{potion}", potionType.getName())
                        .replace("{amplifier}", String.valueOf(amplifier))
                        .replace("{duration}", String.valueOf(duration));
                killer.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            } else {
                killer.sendMessage(ChatColor.RED + "⚠ Invalid potion type: " + type);
                plugin.getLogger().warning("Invalid potion type in config: " + type);
            }
        }
    }

    private static PotionEffectType getPotionEffectTypeByName(String name) {
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type != null && type.getName() != null && type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
