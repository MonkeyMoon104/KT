package com.monkey.kt.effects.list.enchantcolumn;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.enchantcolumn.animation.*;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class EnchantColumnEffect implements KillEffect {

    private final KT plugin;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;

    public EnchantColumnEffect(KT plugin) {
        this.plugin = plugin;

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("effects.enchantcolumn.effectexplosion");
        if (section != null) {
            String typeName = section.getString("type", "REGENERATION");
            this.effectType = PotionEffectType.getByName(typeName.toUpperCase());
            this.amplifier = section.getInt("amplifier", 1);
            this.duration = section.getInt("duration", 10);
        } else {
            this.effectType = PotionEffectType.REGENERATION;
            this.amplifier = 1;
            this.duration = 10;
        }
    }

    @Override
    public void play(Player killer, Location target) {
        World world = target.getWorld();
        if (world == null) return;

        new EnchantAnimation(plugin, world, target, killer, effectType, amplifier, duration).start();
    }
}
