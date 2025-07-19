package com.monkey.kt.listener;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.monkey.kt.KT;

public class ArrowDamageTracker implements Listener {

    private final KT plugin;

    public ArrowDamageTracker(KT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();

        if (!(victim instanceof LivingEntity)) return;
        LivingEntity entityVictim = (LivingEntity) victim;

        if (!(damager instanceof Arrow)) return;
        Arrow arrow = (Arrow) damager;

        if (!(arrow.getShooter() instanceof Player)) return;

        entityVictim.setMetadata("kt_last_hit_arrow", new FixedMetadataValue(plugin, true));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entityVictim.hasMetadata("kt_last_hit_arrow")) {
                entityVictim.removeMetadata("kt_last_hit_arrow", plugin);
            }
        }, 10L);
    }
}
