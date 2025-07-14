package com.monkey.kt.listener;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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

        if (!(victim instanceof Player player)) return;
        if (!(damager instanceof Arrow arrow)) return;

        if (!(arrow.getShooter() instanceof Player)) return;

        player.setMetadata("kt_last_hit_arrow", new FixedMetadataValue(plugin, true));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.hasMetadata("kt_last_hit_arrow")) {
                player.removeMetadata("kt_last_hit_arrow", plugin);
            }
        }, 40L);
    }
}
