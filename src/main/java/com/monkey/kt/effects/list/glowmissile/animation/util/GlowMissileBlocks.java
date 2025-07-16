package com.monkey.kt.effects.list.glowmissile.animation.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.List;

public class GlowMissileBlocks {

    public static void clearBlocks(List<Location> blocks) {
        for (Location loc : blocks) {
            loc.getBlock().setType(Material.AIR);
        }
    }

    public static void placeMissileBlocks(Location base, Location top, List<Location> missileBlocks) {
        base.getBlock().setType(Material.IRON_BLOCK);
        top.getBlock().setType(Material.IRON_BLOCK);
        missileBlocks.add(base);
        missileBlocks.add(top);

        for (Vector offset : List.of(
                new Vector(1, 0, 0),
                new Vector(-1, 0, 0),
                new Vector(0, 0, 1),
                new Vector(0, 0, -1)
        )) {
            Location fenceLoc = base.clone().add(offset);
            fenceLoc.getBlock().setType(Material.NETHER_BRICK_FENCE);
            missileBlocks.add(fenceLoc);
        }
    }
}
