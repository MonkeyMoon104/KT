package com.monkey.kt.effects.list.stellarcollapse;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.stellarcollapse.animation.StellarCollapseLauncher;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class StellarCollapseEffect implements KillEffect {
    private final KT plugin;

    public StellarCollapseEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) return;
        StellarCollapseLauncher.launch(plugin, loc.clone().add(0.5, 1, 0.5));
    }
}
