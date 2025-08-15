package com.monkey.kt.utils;

import com.monkey.kt.KT;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SoundUtils {

    private final KT plugin;
    private final Map<Player, Long> activeSounds = new HashMap<>();

    public SoundUtils(KT plugin) {
        this.plugin = plugin;
    }

    public void playAuraSound(Player player, Location loc) {
        long now = System.currentTimeMillis();

        if (activeSounds.containsKey(player)) {
            long lastPlayed = activeSounds.get(player);
            if (now - lastPlayed < 15000) return;
        }

        player.playSound(loc, "kt.aurab", SoundCategory.PLAYERS, 1.0f, 1.0f);

        activeSounds.put(player, now);
    }

    public void stopAuraSound(Player player) {
        activeSounds.remove(player);
    }
}
