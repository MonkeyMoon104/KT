package com.monkey.kt.effects.list.grave;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.grave.animation.GraveLauncher;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class GraveEffect implements KillEffect {
    private final KT plugin;

    public GraveEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) return;
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_BREAK_BLOCK, 2, 1);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.LARGE_SMOKE, 10, 1, 1, 1, 0.02, 1L, 10);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.SOUL, 20, 1, 1, 1, 0.02, 1L, 10);

        GraveLauncher.launch(plugin, loc.clone().add(0.5, 0, 0.5), killer);
    }
}