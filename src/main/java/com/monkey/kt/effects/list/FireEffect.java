package com.monkey.kt.effects.list;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class FireEffect implements KillEffect {
    private final KT plugin;
    public FireEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        loc.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 2, 1);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.FLAME, 100, 1.5, 2, 1.5, 0.05, 2L, 10);
    }
}

