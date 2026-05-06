package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.SensitiveBlockUtils;
import com.monkey.kt.utils.WorldGuardUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockExecutor {

    private final KT plugin;

    public BlockExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(List<CustomEffectConfig.BlockData> blocks, Location location,
                        boolean temporary, int restoreDelay) {
        if (blocks == null || blocks.isEmpty() || location.getWorld() == null) {
            return;
        }

        Map<Location, Material> placedBlocks = new HashMap<>();

        for (CustomEffectConfig.BlockData blockData : blocks) {
            Material material = blockData.getMaterial();
            if (material == null) {
                continue;
            }

            String pattern = blockData.getPattern().toUpperCase();

            switch (pattern) {
                case "CIRCLE":
                    placeCircle(location, material, blockData.getRadius(), placedBlocks);
                    break;

                case "SINGLE":
                    Location singleLoc = location.clone().add(
                            blockData.getOffsetX(),
                            blockData.getOffsetY(),
                            blockData.getOffsetZ()
                    );
                    placeBlock(singleLoc, material, placedBlocks);
                    break;

                case "LINE":
                    placeLine(location, material, blockData.getRadius(), placedBlocks);
                    break;

                case "SQUARE":
                    placeSquare(location, material, blockData.getRadius(), placedBlocks);
                    break;

                default:
                    plugin.getLogger().warning("Unknown block pattern: " + pattern);
            }
        }

        if (temporary && !placedBlocks.isEmpty()) {
            SchedulerWrapper.runTaskLater(plugin, () -> {
                restoreBlocks(placedBlocks);
            }, restoreDelay);
        }
    }

    private void placeBlock(Location loc, Material material, Map<Location, Material> placedBlocks) {
        Block block = loc.getBlock();

        if (SensitiveBlockUtils.isSensitive(block)) {
            return;
        }

        Material original = block.getType();
        placedBlocks.put(loc.clone(), original);

        WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
            block.setType(material);
        });

        TempBlockStorage.saveTempBlock(loc, original);
    }

    private void placeCircle(Location center, Material material, double radius, Map<Location, Material> placedBlocks) {
        int points = (int) (radius * 8);

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location blockLoc = center.clone().add(x, 0, z);
            blockLoc.setY(center.getWorld().getHighestBlockYAt(blockLoc));

            placeBlock(blockLoc, material, placedBlocks);
        }
    }

    private void placeLine(Location start, Material material, double length, Map<Location, Material> placedBlocks) {
        int blocks = (int) length;

        for (int i = 0; i < blocks; i++) {
            Location blockLoc = start.clone().add(i, 0, 0);
            placeBlock(blockLoc, material, placedBlocks);
        }
    }

    private void placeSquare(Location center, Material material, double size, Map<Location, Material> placedBlocks) {
        int halfSize = (int) (size / 2);

        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                if (Math.abs(x) == halfSize || Math.abs(z) == halfSize) {
                    Location blockLoc = center.clone().add(x, 0, z);
                    blockLoc.setY(center.getWorld().getHighestBlockYAt(blockLoc));
                    placeBlock(blockLoc, material, placedBlocks);
                }
            }
        }
    }

    private void restoreBlocks(Map<Location, Material> blocks) {
        for (Map.Entry<Location, Material> entry : blocks.entrySet()) {
            Location loc = entry.getKey();
            Material original = entry.getValue();

            WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                loc.getBlock().setType(original);
            });

            TempBlockStorage.removeTempBlock(loc);
        }

        TempBlockStorage.flush();
    }
}