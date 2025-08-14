package com.monkey.kt.listener;

import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Entity;

public class ProjectileProtListener implements Listener {

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

    @EventHandler
    public void onFireChargeDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof SmallFireball) {
            SmallFireball fireball = (SmallFireball) damager;

            if (fireball.hasMetadata("kt_phoenix_firecharge")) {
                if (fireball.hasMetadata("kt_phoenix_damage_enabled")) {
                    if (fireball.hasMetadata("kt_phoenix_damage_value")) {
                        MetadataValue damageMetadata = fireball.getMetadata("kt_phoenix_damage_value").get(0);
                        double damageValue = damageMetadata.asDouble();

                        event.setDamage(damageValue);

                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFireChargeExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof SmallFireball) {
            SmallFireball fireball = (SmallFireball) entity;

            if (fireball.hasMetadata("kt_phoenix_firecharge")) {
                event.blockList().clear();
            }
        }
    }
}