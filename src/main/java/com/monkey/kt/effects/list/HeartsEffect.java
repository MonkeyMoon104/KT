package com.monkey.kt.effects.list;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class HeartsEffect implements KillEffect {
    private final KT plugin;
    public HeartsEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_VILLAGER_CELEBRATE, 2, 1);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.HEART, 20, 1, 1.5, 1, 0.1, 2L, 8);
    }
}

