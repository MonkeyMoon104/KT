package com.monkey.kt.boost;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuraBoostManager {

    private final Map<UUID, DamageBoostData> damageBoosts = new HashMap<>();

    public void applyDamageBoost(Player player, int baseAmplifier, int durationSeconds) {
        DamageBoostData current = damageBoosts.get(player.getUniqueId());

        int newAmplifier;
        if (current != null && System.currentTimeMillis() <= current.expiry) {
            newAmplifier = current.amplifier * 2;
        } else {
            newAmplifier = baseAmplifier;
        }

        long newExpiry = System.currentTimeMillis() + (durationSeconds * 1000L);
        damageBoosts.put(player.getUniqueId(), new DamageBoostData(newAmplifier, newExpiry));
    }


    public boolean hasDamageBoost(Player player) {
        DamageBoostData data = damageBoosts.get(player.getUniqueId());
        if (data == null) return false;

        if (System.currentTimeMillis() > data.expiry) {
            damageBoosts.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public int getDamageAmplifier(Player player) {
        if (!hasDamageBoost(player)) return 1;
        return damageBoosts.get(player.getUniqueId()).amplifier;
    }

    private static class DamageBoostData {
        int amplifier;
        long expiry;

        DamageBoostData(int amplifier, long expiry) {
            this.amplifier = amplifier;
            this.expiry = expiry;
        }
    }
}
