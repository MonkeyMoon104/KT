package com.monkey.kt.effects.list.wither;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.wither.animation.WitherLauncher;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WitherEffect implements KillEffect {
    private final KT plugin;

    public WitherEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) return;
        WitherLauncher.launch(plugin, killer, loc.clone().add(0.5, 1, 0.5));
    }
}
