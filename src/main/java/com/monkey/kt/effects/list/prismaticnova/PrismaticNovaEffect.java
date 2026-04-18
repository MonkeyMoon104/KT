package com.monkey.kt.effects.list.prismaticnova;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.prismaticnova.animation.PrismaticNovaAnimation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PrismaticNovaEffect implements KillEffect {

    private final KT plugin;

    public PrismaticNovaEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) {
            return;
        }
        PrismaticNovaAnimation.launch(plugin, loc.clone().add(0.5, 1.0, 0.5), killer);
    }
}
