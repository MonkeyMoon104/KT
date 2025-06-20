package com.monkey.kt.effects;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface KillEffect {
    void play(Player player, Location loc);
}
