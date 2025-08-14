package com.monkey.kt.effects.list.firephoenix;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.firephoenix.animation.FirePhoenixLauncher;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FirePhoenixEffect implements KillEffect {
    private final KT plugin;

    public FirePhoenixEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) return;
        FirePhoenixLauncher.launch(plugin, loc.clone().add(0.5, 1, 0.5), killer);
    }
}
