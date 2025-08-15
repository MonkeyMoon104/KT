package com.monkey.kt.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.MetadataValue;

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

    @EventHandler
    public void onTNTDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) damager;

            if (tnt.hasMetadata("kt_explosion_tnt")) {
                if (tnt.hasMetadata("kt_explosion_damage_enabled")) {
                    if (tnt.hasMetadata("kt_explosion_damage_value")) {
                        if (tnt.hasMetadata("kt_explosion_damage_rage")) {
                            MetadataValue enabledMetadata = tnt.getMetadata("kt_explosion_damage_enabled").get(0);
                            MetadataValue valueMetadata = tnt.getMetadata("kt_explosion_damage_value").get(0);
                            MetadataValue rageMetadata = tnt.getMetadata("kt_explosion_damage_rage").get(0);

                            boolean damageEnabled = enabledMetadata.asBoolean();
                            double damageValue = valueMetadata.asDouble();
                            double damageRage = rageMetadata.asDouble();

                            if (damageEnabled) {
                                World world = tnt.getWorld();
                                Location center = tnt.getLocation();

                                Player killer = null;
                                if (tnt.hasMetadata("kt_explosion_killer")) {
                                    String killerUUID = tnt.getMetadata("kt_explosion_killer").get(0).asString();
                                    killer = Bukkit.getPlayer(java.util.UUID.fromString(killerUUID));
                                }

                                for (Entity entity : world.getNearbyEntities(center, damageRage, damageRage, damageRage)) {
                                    if (entity.equals(killer)) continue;
                                    if (entity instanceof LivingEntity) {
                                        ((LivingEntity) entity).damage(damageValue, tnt);
                                    }
                                }

                            } else {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTNTExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) entity;
            if (tnt.hasMetadata("kt_explosion_tnt")) {
                event.blockList().clear();
                event.setCancelled(true);
            }
        }
    }
}