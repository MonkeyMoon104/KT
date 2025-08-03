package com.monkey.kt.effects.list.aurafarming;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.aurafarming.animation.AuraTrailAnimation;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class AuraFarmingEffect implements KillEffect {

    private final KT plugin;

    public AuraFarmingEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) return;

        loc.getWorld().playSound(loc, "kt.aurab", SoundCategory.PLAYERS, 1.0f, 1.0f);

        new AuraTrailAnimation(plugin, loc.clone(), killer).start();
    }
}
