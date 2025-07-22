package com.monkey.kt.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityByPassSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        EntityType type = event.getEntityType();

        if (event.getEntity().hasMetadata("kt_bypass_spawn")) {
            event.setCancelled(false);
            Bukkit.getLogger().info("[KT Bypass] Bypass " + type.name() + " forced spawn.");
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata("kt_bypass_spawn")) {
            EntityType type = event.getEntityType();
            if (type == EntityType.WARDEN || type == EntityType.WITHER) {
                event.setCancelled(true);
            }
        }
    }
}
