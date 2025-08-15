package com.monkey.kt.effects.list.totem;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class TotemEffect implements KillEffect {
    private final KT plugin;
    public TotemEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player killer, Location loc) {
        loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 2.0f, 1);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.TOTEM_OF_UNDYING, 300, 1.8, 2.5, 1.8, 0.3, 2L, 10);
        DamageConfig damageConfig = DamageUtils.getDamageConfig("totem", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, loc, damageConfig.getRadius(), damageConfig.getValue());
        }
    }
}
