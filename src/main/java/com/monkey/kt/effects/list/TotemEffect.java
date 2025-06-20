package com.monkey.kt.effects.list;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class TotemEffect implements KillEffect {
    private final KT plugin;
    public TotemEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 2.0f, 1);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.TOTEM, 300, 1.8, 2.5, 1.8, 0.3, 2L, 10);
    }
}
