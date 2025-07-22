package com.monkey.kt.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class WorldGuardUtils {

    private static boolean available = false;

    public static void setup() {
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        available = wg != null && wg.isEnabled();
    }

    public static boolean isAvailable() {
        return available;
    }

    public static WorldGuard getWorldGuard() {
        return available ? WorldGuard.getInstance() : null;
    }

    public static void runWithWorldGuardBypass(Location location, Runnable task) {
        if (isAvailable()) {
            try {
                com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(location);
                RegionContainer container = WorldGuardUtils.getWorldGuard().getPlatform().getRegionContainer();
                RegionQuery query = container.createQuery();
                if (query.testState(weLoc, null, Flags.BUILD)) {
                    task.run();
                } else {
                    task.run();
                }
            } catch (Exception e) {
                task.run();
            }
        } else {
            task.run();
        }
    }
}
