package com.monkey.kt.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Entity;

public class WitherSkullProtectionListener implements Listener {


    @EventHandler
    public void onSkullDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof WitherSkull) {
            WitherSkull skull = (WitherSkull) damager;

            if (skull.hasMetadata("kt_wither_skull")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSkullExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof WitherSkull) {
            WitherSkull skull = (WitherSkull) entity;

            if (skull.hasMetadata("kt_wither_skull")) {
                event.blockList().clear();
            }
        }
    }
}

