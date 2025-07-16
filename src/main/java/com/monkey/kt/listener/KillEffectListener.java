package com.monkey.kt.listener;

import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.storage.EffectStorage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;


public class KillEffectListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();

        Player killer = victim.getKiller();
        if (killer == null || !killer.isOnline()) return;

        String effectName = EffectStorage.getEffect(killer);
        if (effectName == null) return;

        if (effectName.equalsIgnoreCase("sniper")) {
            if (!victim.hasMetadata("kt_last_hit_arrow")) return;
        }

        KillEffect effect = KillEffectFactory.getEffect(effectName);
        if (effect == null) return;

        effect.play(killer, victim.getLocation());
    }


    @EventHandler
    public void onPigDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Pig && entity.hasMetadata("kt_pigstep")) {
            event.setCancelled(true);
        }
    }
}
