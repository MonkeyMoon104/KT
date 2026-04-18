package com.monkey.kt.effects.list.voidlotus;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.voidlotus.animation.VoidLotusAnimation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class VoidLotusEffect implements KillEffect {

    private final KT plugin;

    public VoidLotusEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        if (loc.getWorld() == null) {
            return;
        }
        VoidLotusAnimation.launch(plugin, loc.clone().add(0.5, 0.2, 0.5), killer);
    }
}
