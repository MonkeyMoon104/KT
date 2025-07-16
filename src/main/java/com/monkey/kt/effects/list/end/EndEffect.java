package com.monkey.kt.effects.list.end;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EndEffect implements KillEffect {
    private final KT plugin;
    public EndEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 2, 1);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.PORTAL, 150, 1.5, 1.5, 1.5, 0.2, 2L, 10);
    }
}

