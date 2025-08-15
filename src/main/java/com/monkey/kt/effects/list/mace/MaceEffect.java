package com.monkey.kt.effects.list.mace;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.mace.animation.MaceLauncher;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MaceEffect implements KillEffect {
    private final KT plugin;

    public MaceEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) return;
        MaceLauncher.launch(plugin, killer, loc.clone().add(0.5, 1, 0.5));
    }
}
