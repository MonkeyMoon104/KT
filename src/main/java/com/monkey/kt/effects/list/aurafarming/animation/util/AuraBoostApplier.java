package com.monkey.kt.effects.list.aurafarming.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.utils.potion.PotionEffectUtils;
import com.monkey.kt.utils.text.TextUtils;
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

            String message = section.getString("damage-message", "&bYou received a damage boost for {duration}s (Amplifier: {amplifier})!");
            message = message.replace("{duration}", String.valueOf(duration));
            message = message.replace("{amplifier}", String.valueOf(currentAmplifier));
            killer.sendMessage(TextUtils.legacySection(message));
        }

        ConfigurationSection potionBoost = section.getConfigurationSection("potion");
        if (potionBoost != null) {
            String type = potionBoost.getString("type", "REGENERATION");
            int amplifier = potionBoost.getInt("amplifier", 1);
            int duration = potionBoost.getInt("duration", 10);

            PotionEffectType potionType = PotionEffectUtils.fromName(type);
            if (potionType != null) {
                killer.addPotionEffect(new PotionEffect(potionType, duration * 20, amplifier - 1, true, true));

                String message = section.getString("potion-message", "&dYou received {potion} {amplifier} for {duration}s!");
                message = message
                        .replace("{potion}", PotionEffectUtils.displayName(potionType))
                        .replace("{amplifier}", String.valueOf(amplifier))
                        .replace("{duration}", String.valueOf(duration));
                killer.sendMessage(TextUtils.legacySection(message));
            } else {
                killer.sendMessage(TextUtils.legacySection("&cInvalid potion type: " + type));
                plugin.getLogger().warning("Invalid potion type in config: " + type);
            }
        }
    }
}
