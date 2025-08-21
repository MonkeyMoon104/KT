package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class KillEffectDamageTracker implements Listener {

    private final KT plugin;

    public KillEffectDamageTracker(KT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();

        if (!(victim instanceof LivingEntity)) return;
        LivingEntity entityVictim = (LivingEntity) victim;

        if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                entityVictim.setMetadata("kt_last_hit_arrow", new FixedMetadataValue(plugin, true));
                removeMetaLater(entityVictim, "kt_last_hit_arrow");
            }
            return;
        }

        if (damager instanceof Player) {
            Player player = (Player) damager;
            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (weapon != null && weapon.getType() == Material.MACE) {
                entityVictim.setMetadata("kt_last_hit_mace", new FixedMetadataValue(plugin, true));
                removeMetaLater(entityVictim, "kt_last_hit_mace");
            }
        }
    }

    private void removeMetaLater(final LivingEntity entity, final String key) {
        SchedulerWrapper.runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (entity.hasMetadata(key)) {
                    entity.removeMetadata(key, plugin);
                }
            }
        }, 10L);
    }
}
