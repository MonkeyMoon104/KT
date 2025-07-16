package com.monkey.kt.effects.list.lightning;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class LightningEffect implements KillEffect {

    private final KT plugin;
    public LightningEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        loc.getWorld().strikeLightningEffect(loc);

        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.8f, 1f);

        EffectUtils.playRepeatingParticle(plugin, loc, Particle.CRIT_MAGIC, 150, 1.2, 1.5, 1.2, 0.2, 2L, 5);
    }
}
