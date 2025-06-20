package com.monkey.kt.effects.list;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class NotesEffect implements KillEffect {
    private final KT plugin;
    public NotesEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player player, Location loc) {
        loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.NOTE, 40, 1.5, 1.5, 1.5, 1, 2L, 8);
    }
}

