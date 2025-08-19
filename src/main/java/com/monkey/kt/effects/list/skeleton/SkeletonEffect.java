package com.monkey.kt.effects.list.skeleton;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.skeleton.animation.SkeletonAnimation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SkeletonEffect implements KillEffect {

    private final KT plugin;

    public SkeletonEffect(KT plugin) {
        this.plugin = plugin;
    }

    public void play(Player deadPlayer, Location loc) {
        new SkeletonAnimation(plugin, loc, deadPlayer).start();
    }
}

// Made by: Dominikhun250