package com.monkey.kt.effects.list.explosion.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.explosion.animation.util.ExplosionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ExplosionAnimation {

    public static void launchExplosionTNT(KT plugin, Location center, Player killer) {
        for (int i = 0; i < 6; i++) {
            ExplosionUtils.launchCosmeticTNT(plugin, center, i);
        }
    }
}
