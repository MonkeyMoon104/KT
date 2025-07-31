package com.monkey.kt.effects.list.cryocore.animation.util;

import com.monkey.kt.utils.SensitiveBlockUtils;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CryoCoreStructures {

    public static class BlockStateHolder {
        public final Map<Location, Material> originalBlocks = new HashMap<>();
        public final Set<Location> iceSpikeBlocks = new HashSet<>();
    }

    public static void randomlyPlaceSnowBlocks(Location center, int radius, BlockStateHolder holder, int batch, boolean allowStructure) {
        if (!allowStructure) return;
        World world = center.getWorld();
        if (world == null) return;

        Random random = new Random(batch);

        int placed = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (placed >= 92) return;
                double dist = Math.sqrt(x * x + z * z);
                if (dist > radius) continue;

                if (random.nextDouble() > 0.65) {
                    int surfaceY = world.getHighestBlockYAt(center.getBlockX() + x, center.getBlockZ() + z);
                    Location surfaceLoc = new Location(world, center.getX() + x, surfaceY, center.getZ() + z);
                    Block targetBlock = surfaceLoc.getBlock();
                    Block above = surfaceLoc.clone().add(0, 1, 0).getBlock();

                    if (SensitiveBlockUtils.isSensitive(targetBlock) || SensitiveBlockUtils.isSensitive(above)) {
                        continue;
                    }

                    if (!holder.originalBlocks.containsKey(targetBlock.getLocation())) {
                        holder.originalBlocks.put(targetBlock.getLocation(), targetBlock.getType());

                        WorldGuardUtils.runWithWorldGuardBypass(targetBlock.getLocation(), () -> {
                            targetBlock.setType(Material.SNOW_BLOCK);
                        });
                        placed++;
                    }
                }
            }
        }
    }

    public static void restoreGround(BlockStateHolder holder) {
        for (Map.Entry<Location, Material> entry : holder.originalBlocks.entrySet()) {
            Location loc = entry.getKey();
            Material originalMat = entry.getValue();

            WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                Block block = loc.getBlock();
                if (block.getType() == Material.SNOW_BLOCK) {
                    block.setType(originalMat);
                }
            });
        }

        for (Location loc : holder.iceSpikeBlocks) {
            WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                if (loc.getBlock().getType() == Material.PACKED_ICE) {
                    loc.getBlock().setType(Material.AIR);
                }
            });
        }
    }

    public static void spawnLargeIceSpikesBorder(Plugin plugin, Location center, int radius, BlockStateHolder holder, boolean allowStructure) {
        if (!allowStructure) return;
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int angle = 0; angle < 360; angle += 6) {
                    double rad = Math.toRadians(angle);
                    double baseX = Math.cos(rad) * radius;
                    double baseZ = Math.sin(rad) * radius;
                    Location base = center.clone().add(baseX, 0, baseZ);

                    int height = 12 + new Random().nextInt(6);
                    double inclinationFactor = 0.4 + new Random().nextDouble() * 0.6;

                    WorldGuardUtils.runWithWorldGuardBypass(base, () -> {
                        for (int y = 0; y < height; y++) {
                            double inclineX = baseX * inclinationFactor * y / height;
                            double inclineZ = baseZ * inclinationFactor * y / height;
                            Location spikeLoc = base.clone().add(inclineX, y, inclineZ);
                            Block block = spikeLoc.getBlock();

                            Block above = block.getLocation().clone().add(0, 1, 0).getBlock();
                            if (SensitiveBlockUtils.isSensitive(block) || SensitiveBlockUtils.isSensitive(above)) {
                                continue;
                            }

                            if (block.getType().isAir() || block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK) {
                                block.setType(Material.PACKED_ICE);
                                holder.iceSpikeBlocks.add(spikeLoc.getBlock().getLocation());
                            }
                        }
                    });
                }
            }
        }.runTask(plugin);
    }
}
