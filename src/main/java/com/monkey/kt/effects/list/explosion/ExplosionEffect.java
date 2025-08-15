package com.monkey.kt.effects.list.explosion;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.explosion.animation.ExplosionAnimation;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ExplosionEffect implements KillEffect {

    private final KT plugin;

    public ExplosionEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);
        ExplosionAnimation.launchExplosionTNT(plugin, loc, killer);
    }
}
