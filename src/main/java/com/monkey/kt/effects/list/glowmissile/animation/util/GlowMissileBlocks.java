package com.monkey.kt.effects.list.glowmissile.animation.util;

import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.Location;
import org.bukkit.Material;
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
        WorldGuardUtils.runWithWorldGuardBypass(base, () -> {
            Material originalBase = base.getBlock().getType();
            base.getBlock().setType(Material.IRON_BLOCK);
            TempBlockStorage.saveTempBlock(base, originalBase);
            missileBlocks.add(base);
        });

        WorldGuardUtils.runWithWorldGuardBypass(top, () -> {
            Material originalTop = top.getBlock().getType();
            top.getBlock().setType(Material.IRON_BLOCK);
            TempBlockStorage.saveTempBlock(top, originalTop);
            missileBlocks.add(top);
        });

        for (Vector offset : Arrays.asList(
                new Vector(1, 0, 0),
                new Vector(-1, 0, 0),
                new Vector(0, 0, 1),
                new Vector(0, 0, -1)
        )) {
            Location fenceLoc = base.clone().add(offset);
            WorldGuardUtils.runWithWorldGuardBypass(fenceLoc, () -> {
                Material originalFence = fenceLoc.getBlock().getType();
                fenceLoc.getBlock().setType(Material.NETHER_BRICK_FENCE);
                TempBlockStorage.saveTempBlock(fenceLoc, originalFence);
                missileBlocks.add(fenceLoc);
            });
        }
        TempBlockStorage.flush();
    }
}
