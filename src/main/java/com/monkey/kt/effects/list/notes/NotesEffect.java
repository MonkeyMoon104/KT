package com.monkey.kt.effects.list.notes;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class NotesEffect implements KillEffect {
    private final KT plugin;
    public NotesEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player killer, Location loc) {
        loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.NOTE, 40, 1.5, 1.5, 1.5, 1, 2L, 8);
        DamageConfig damageConfig = DamageUtils.getDamageConfig("notes", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, loc, damageConfig.getRadius(), damageConfig.getValue());
        }
    }
}

