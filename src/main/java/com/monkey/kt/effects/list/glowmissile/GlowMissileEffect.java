package com.monkey.kt.effects.list.glowmissile;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.glowmissile.animation.GlowMissileLauncher;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GlowMissileEffect implements KillEffect {
    private final KT plugin;

    public GlowMissileEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) return;

        GlowMissileLauncher.launch(plugin, loc.getBlock().getLocation().add(0.5, 1, 0.5), killer);
    }
}
