package com.monkey.kt.listener;

import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.storage.EffectStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillEffectListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || !killer.isOnline()) return;

        String effectName = EffectStorage.getEffect(killer);
        if (effectName == null) return;

        KillEffect effect = KillEffectFactory.getEffect(effectName);
        if (effect == null) return;

        effect.play(killer, victim.getLocation());
    }
}
