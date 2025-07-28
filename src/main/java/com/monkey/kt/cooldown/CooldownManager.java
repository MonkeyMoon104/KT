package com.monkey.kt.cooldown;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private final Map<UUID, Long> mobCooldowns = new HashMap<>();

    public boolean isOnCooldown(Player player, boolean isMobKill, int delay) {
        long now = System.currentTimeMillis();
        Map<UUID, Long> map = isMobKill ? mobCooldowns : playerCooldowns;

        long lastUsed = map.getOrDefault(player.getUniqueId(), 0L);
        return now - lastUsed < delay * 1000L;
    }

    public void setCooldown(Player player, boolean isMobKill) {
        long now = System.currentTimeMillis();
        if (isMobKill) {
            mobCooldowns.put(player.getUniqueId(), now);
        } else {
            playerCooldowns.put(player.getUniqueId(), now);
        }
    }
}
