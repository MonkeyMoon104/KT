package com.monkey.kt.effects.list.cloud;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.util.EffectUtils;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CloudEffect implements KillEffect {
    private final KT plugin;
    public CloudEffect(KT plugin) { this.plugin = plugin; }

    @Override
    public void play(Player killer, Location loc) {
        loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 2, 1);
        EffectUtils.playRepeatingParticle(plugin, loc, Particle.CLOUD, 100, 1.5, 1.5, 1.5, 0.02, 2L, 10);
        DamageConfig damageConfig = DamageUtils.getDamageConfig("cloud", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, loc, damageConfig.getRadius(), damageConfig.getValue());
        }
    }
}

