package com.monkey.kt.effects.list;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class CloudEffect implements KillEffect {
    private final KT plugin;
    public CloudEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.CLOUD, 100, 1.5, 1.5, 1.5, 0.02, 2L, 10);
    }
}

