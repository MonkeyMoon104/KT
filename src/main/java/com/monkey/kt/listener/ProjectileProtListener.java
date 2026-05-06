package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.utils.entity.EntityDataUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ProjectileProtListener implements Listener {

    private final KT plugin;

    public ProjectileProtListener(KT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSkullDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof WitherSkull skull) {
            if (EntityDataUtils.hasBoolean(skull, plugin, "kt_wither_skull")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSkullExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof WitherSkull skull) {
            if (EntityDataUtils.hasBoolean(skull, plugin, "kt_wither_skull")) {
                event.blockList().clear();
            }
        }
    }

    @EventHandler
    public void onFireChargeDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof SmallFireball fireball) {
            if (EntityDataUtils.hasBoolean(fireball, plugin, "kt_phoenix_firecharge")) {
                if (EntityDataUtils.hasBoolean(fireball, plugin, "kt_phoenix_damage_enabled")) {
                    double damageValue = EntityDataUtils.getDouble(fireball, plugin, "kt_phoenix_damage_value", 0.0);
                    event.setDamage(damageValue);
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFireChargeExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof SmallFireball fireball) {
            if (EntityDataUtils.hasBoolean(fireball, plugin, "kt_phoenix_firecharge")) {
                event.blockList().clear();
            }
        }
    }

    @EventHandler
    public void onTNTDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof TNTPrimed tnt) {
            if (EntityDataUtils.hasBoolean(tnt, plugin, "kt_explosion_tnt")
                    && EntityDataUtils.hasBoolean(tnt, plugin, "kt_explosion_damage_enabled")
                    && EntityDataUtils.hasDouble(tnt, plugin, "kt_explosion_damage_value")
                    && EntityDataUtils.hasDouble(tnt, plugin, "kt_explosion_damage_rage")) {

                boolean damageEnabled = EntityDataUtils.getBoolean(tnt, plugin, "kt_explosion_damage_enabled", false);
                double damageValue = EntityDataUtils.getDouble(tnt, plugin, "kt_explosion_damage_value", 0.0);
                double damageRage = EntityDataUtils.getDouble(tnt, plugin, "kt_explosion_damage_rage", 0.0);

                if (damageEnabled) {
                    World world = tnt.getWorld();
                    Location center = tnt.getLocation();

                    Player killer = null;
                    if (EntityDataUtils.hasString(tnt, plugin, "kt_explosion_killer")) {
                        String killerUUID = EntityDataUtils.getString(tnt, plugin, "kt_explosion_killer");
                        killer = Bukkit.getPlayer(java.util.UUID.fromString(killerUUID));
                    }

                    for (Entity entity : world.getNearbyEntities(center, damageRage, damageRage, damageRage)) {
                        if (entity.equals(killer)) continue;
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.damage(damageValue, tnt);
                        }
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTNTExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof TNTPrimed tnt) {
            if (EntityDataUtils.hasBoolean(tnt, plugin, "kt_explosion_tnt")) {
                event.blockList().clear();
                event.setCancelled(true);
            }
        }
    }
}
