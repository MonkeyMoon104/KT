package com.monkey.kt.effects.list.pigstep;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.list.pigstep.animation.util.PigstepFlameTask;
import com.monkey.kt.effects.list.pigstep.animation.util.PigstepPigSpawner;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PigstepEffect implements KillEffect {
    private final KT plugin;

    public PigstepEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        new PigstepFlameTask(plugin, loc).runTaskTimer(plugin, 0L, 2L);
        new PigstepPigSpawner(plugin, killer, loc).runTask(plugin);
    }
}
