package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class EntityExecutor {

    private final KT plugin;

    public EntityExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(List<CustomEffectConfig.EntityData> entities, Location location) {
        if (entities == null || entities.isEmpty() || location.getWorld() == null) {
            return;
        }

        for (CustomEffectConfig.EntityData entityData : entities) {
            if (entityData.getDelay() > 0) {
                SchedulerWrapper.runTaskLater(plugin, () -> {
                    spawnEntity(entityData, location);
                }, entityData.getDelay());
            } else {
                spawnEntity(entityData, location);
            }
        }
    }

    private void spawnEntity(CustomEffectConfig.EntityData data, Location location) {
        String type = data.getType().toUpperCase();

        if (type.equals("LIGHTNING")) {
            if (data.isEffectOnly()) {
                location.getWorld().strikeLightningEffect(location);
            } else {
                location.getWorld().strikeLightning(location);
            }
            return;
        }

        try {
            EntityType entityType = EntityType.valueOf(type);

            Entity entity = location.getWorld().spawnEntity(location, entityType);

            entity.setMetadata("kt_custom_entity", new FixedMetadataValue(plugin, true));
            entity.setMetadata("kt_bypass_spawn", new FixedMetadataValue(plugin, true));

            if (!data.getCustomName().isEmpty()) {
                entity.setCustomName(ChatColor.translateAlternateColorCodes('&', data.getCustomName()));
                entity.setCustomNameVisible(true);
            }

            entity.setGravity(data.hasGravity());
            entity.setInvulnerable(data.isInvulnerable());

            if (entity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) entity;
                armorStand.setVisible(data.isVisible());
                armorStand.setMarker(true);
            }

            int duration = data.getDuration();
            if (duration > 0) {
                SchedulerWrapper.runTaskLater(plugin, () -> {
                    if (entity.isValid() && !entity.isDead()) {
                        entity.remove();
                    }
                }, duration);
            }

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid entity type: " + type);
        }
    }
}