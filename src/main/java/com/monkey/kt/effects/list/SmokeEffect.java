package com.monkey.kt.effects.list;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class SmokeEffect implements KillEffect {
    private final KT plugin;
    public SmokeEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.SMOKE_LARGE, 80, 1, 1, 1, 0.02, 2L, 10);
    }
}

