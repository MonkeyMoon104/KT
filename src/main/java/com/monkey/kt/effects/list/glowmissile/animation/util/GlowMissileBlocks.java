package com.monkey.kt.effects.list.glowmissile.animation.util;

import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class GlowMissileBlocks {

    public static void clearBlocks(List<Location> blocks) {
        for (Location loc : blocks) {
            WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                loc.getBlock().setType(Material.AIR);
                TempBlockStorage.removeTempBlock(loc);
            });
        }
        blocks.clear();
        TempBlockStorage.flush();
    }

    public static void placeMissileBlocks(Location base, Location top, List<Location> missileBlocks) {
        base = findAirAbove(base);
        top = base.clone().add(0, 1, 0);

        for (Location loc : Arrays.asList(base, top)) {
            WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                if (loc.getBlock().getType() == Material.AIR) {
                    Material original = loc.getBlock().getType();
                    loc.getBlock().setType(Material.IRON_BLOCK);
                    TempBlockStorage.saveTempBlock(loc, original);
                    missileBlocks.add(loc);
                }
            });
        }

        for (Vector offset : Arrays.asList(
                new Vector(1, 0, 0),
                new Vector(-1, 0, 0),
                new Vector(0, 0, 1),
                new Vector(0, 0, -1)
        )) {
            Location fenceLoc = base.clone().add(offset);
            fenceLoc = findAirAbove(fenceLoc);
            Location finalFenceLoc = fenceLoc;
            WorldGuardUtils.runWithWorldGuardBypass(fenceLoc, () -> {
                if (finalFenceLoc.getBlock().getType() == Material.AIR) {
                    Material originalFence = finalFenceLoc.getBlock().getType();
                    finalFenceLoc.getBlock().setType(Material.NETHER_BRICK_FENCE);
                    TempBlockStorage.saveTempBlock(finalFenceLoc, originalFence);
                    missileBlocks.add(finalFenceLoc);
                }
            });
        }
        TempBlockStorage.flush();
    }

    private static Location findAirAbove(Location loc) {
        Location temp = loc.clone();
        World world = loc.getWorld();
        if (world == null) return loc;

        while (temp.getBlock().getType() != Material.AIR) {
            temp.add(0, 1, 0);
            if (temp.getY() > world.getMaxHeight()) break;
        }
        return temp;
    }
}
