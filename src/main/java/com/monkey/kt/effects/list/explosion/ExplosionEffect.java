package com.monkey.kt.effects.list.explosion;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ExplosionEffect implements KillEffect {
    private final KT plugin;
    public ExplosionEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
        loc.getWorld().createExplosion(loc, 0F, false);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.EXPLOSION_LARGE, 50, 1, 1, 1, 0.1, 2L, 5);
    }
}

