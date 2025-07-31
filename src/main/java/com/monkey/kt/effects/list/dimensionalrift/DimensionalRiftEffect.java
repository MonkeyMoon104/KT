package com.monkey.kt.effects.list.dimensionalrift;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.dimensionalrift.animation.DimensionalRiftLauncher;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DimensionalRiftEffect implements KillEffect {
    private final KT plugin;

    public DimensionalRiftEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) return;
        DimensionalRiftLauncher.launch(plugin, loc.clone().add(0.5, 1, 0.5));
    }
}
