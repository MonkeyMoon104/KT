package com.monkey.kt.effects.list.cryocore.animation.util.structures.placer;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.helper.BlockStateHolder;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.SensitiveBlockUtils;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class IceSpikePlacer {

    private final KT plugin;

    public IceSpikePlacer(KT plugin) {
        this.plugin = plugin;
    }

    public void spawnLargeIceSpikesBorder(Location center, int radius, BlockStateHolder holder,
                                          boolean allowStructure) {
        if (!allowStructure) return;
        World world = center.getWorld();
        if (world == null) return;

        final List<Location> blocksToPlace = new ArrayList<>();
        final Map<Location, Integer> spikeHeights = new HashMap<>();
        Random random = new Random();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            generateSpikeLocations(center, radius, blocksToPlace, spikeHeights, random);

            Bukkit.getScheduler().runTask(plugin, () -> {
                startOptimizedSpikePlacement(holder, blocksToPlace, spikeHeights, center, random);
            });
        });
    }

    private void generateSpikeLocations(Location center, int radius, List<Location> blocksToPlace,
                                        Map<Location, Integer> spikeHeights, Random random) {
        for (int angle = 0; angle < 360; angle += 28) {
            double rad = Math.toRadians(angle);
            double baseX = Math.cos(rad) * radius;
            double baseZ = Math.sin(rad) * radius;
            Location base = center.clone().add(baseX, 0, baseZ);

            int height = 16 + random.nextInt(8);
            double inclinationFactor = 1.0 + random.nextDouble() * 0.8;

            for (int y = 0; y < height; y++) {
                double inclineX = baseX * inclinationFactor * (y / (double) height);
                double inclineZ = baseZ * inclinationFactor * (y / (double) height);

                inclineX += (random.nextDouble() - 0.5) * 0.5;
                inclineZ += (random.nextDouble() - 0.5) * 0.5;

                Location spikeLoc = base.clone().add(inclineX, y, inclineZ);
                blocksToPlace.add(spikeLoc);
                spikeHeights.put(spikeLoc, y);

                if (y < height - 2) {
                    double baseRadius = Math.max(1.2, 4.0 - (y * 3.5 / height));

                    for (int dx = -3; dx <= 3; dx++) {
                        for (int dz = -3; dz <= 3; dz++) {
                            if (dx == 0 && dz == 0) continue;
                            double distance = Math.sqrt(dx * dx + dz * dz);
                            if (distance <= baseRadius) {
                                Location thickLoc = spikeLoc.clone().add(dx, 0, dz);
                                blocksToPlace.add(thickLoc);
                                spikeHeights.put(spikeLoc, y);
                            }
                        }
                    }
                }
            }
        }

        Collections.shuffle(blocksToPlace, random);
    }

    private void startOptimizedSpikePlacement(BlockStateHolder holder, List<Location> blocksToPlace,
                                              Map<Location, Integer> spikeHeights, Location center,
                                              Random random) {
        new BukkitRunnable() {
            private final List<Location> spikeLocations = new ArrayList<>(blocksToPlace);
            private int spikeIndex = 0;
            private int dynamicBatchSize = 70;
            private long lastExecutionTime = 0;

            @Override
            public void run() {
                long startTime = System.nanoTime();
                int processed = 0;

                while (spikeIndex < spikeLocations.size() && processed < dynamicBatchSize) {
                    Location spikeLoc = spikeLocations.get(spikeIndex);

                    Block block = holder.getCachedBlock(spikeLoc);
                    Block above = holder.getCachedBlock(spikeLoc.clone().add(0, 1, 0));

                    boolean isSensitive = holder.isSensitiveCached(spikeLoc) ||
                            holder.isSensitiveCached(above.getLocation());

                    if (!isSensitive &&
                            !SensitiveBlockUtils.isSensitive(block) &&
                            !SensitiveBlockUtils.isSensitive(above)) {

                        Material blockType = block.getType();
                        if (blockType.isAir() || blockType == Material.SNOW || blockType == Material.SNOW_BLOCK) {
                            Material originalMat = blockType;

                            int localHeight = spikeHeights.getOrDefault(spikeLoc, 1);
                            double relativeHeight = (spikeLoc.getY() - center.getY()) / (double) localHeight;
                            relativeHeight = Math.min(1.0, Math.max(0.0, relativeHeight));
                            double blueChance = 0.2 + 0.6 * relativeHeight;
                            Material iceMat = (random.nextDouble() < blueChance) ? Material.BLUE_ICE : Material.PACKED_ICE;

                            WorldGuardUtils.runWithWorldGuardBypass(spikeLoc, () -> {
                                block.setType(iceMat);
                            });

                            holder.iceSpikeBlocks.add(spikeLoc);
                            TempBlockStorage.saveTempBlock(spikeLoc, originalMat);
                        }
                    } else {
                        holder.addSensitiveCached(spikeLoc);
                    }

                    spikeIndex++;
                    processed++;
                }

                long executionTime = System.nanoTime() - startTime;
                if (lastExecutionTime > 0) {
                    if (executionTime < 20_000_000) {
                        dynamicBatchSize = Math.min(120, dynamicBatchSize + 8);
                    } else if (executionTime > 40_000_000) {
                        dynamicBatchSize = Math.max(25, dynamicBatchSize - 12);
                    }
                }
                lastExecutionTime = executionTime;

                if (spikeIndex >= spikeLocations.size()) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, TempBlockStorage::flush);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}