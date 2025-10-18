package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PotionExecutor {

    private final KT plugin;

    public PotionExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(Player killer, Location location, CustomEffectConfig config) {
        List<CustomEffectConfig.PotionData> killerPotions = config.getKillerPotions();
        if (killerPotions != null && !killerPotions.isEmpty()) {
            for (CustomEffectConfig.PotionData potionData : killerPotions) {
                applyPotion(killer, potionData);
            }
        }

        List<CustomEffectConfig.PotionData> nearbyPotions = config.getNearbyPotions();
        if (nearbyPotions != null && !nearbyPotions.isEmpty() && location.getWorld() != null) {
            double radius = config.getPotionRadius();

            for (Player player : location.getWorld().getPlayers()) {
                if (player.equals(killer)) {
                    continue;
                }

                if (player.getLocation().distance(location) <= radius) {
                    for (CustomEffectConfig.PotionData potionData : nearbyPotions) {
                        applyPotion(player, potionData);
                    }
                }
            }
        }
    }

    private void applyPotion(Player player, CustomEffectConfig.PotionData potionData) {
        PotionEffectType type = potionData.getType();
        if (type != null) {
            PotionEffect effect = new PotionEffect(
                    type,
                    potionData.getDuration(),
                    potionData.getAmplifier() - 1,
                    true,
                    true
            );
            player.addPotionEffect(effect);
        }
    }
}