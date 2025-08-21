package com.monkey.kt.effects.list.cryocore.animation.util.structures.placer;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.helper.BlockStateHolder;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.WorldGuardUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class StructureRestorer {

    private final KT plugin;

    public StructureRestorer(KT plugin) {
        this.plugin = plugin;
    }

    public void restoreGround(BlockStateHolder holder) {
        final Map<Location, Material> blocksToRestore = new HashMap<>(holder.originalBlocks);
        final Set<Location> spikesToRemove = new HashSet<>(holder.iceSpikeBlocks);

        startOptimizedRestoration(holder, blocksToRestore, spikesToRemove);
    }

    private void startOptimizedRestoration(BlockStateHolder holder,
                                           Map<Location, Material> blocksToRestore,
                                           Set<Location> spikesToRemove) {

        Location centerLoc = null;
        if (!blocksToRestore.isEmpty()) {
            centerLoc = blocksToRestore.keySet().iterator().next();
        } else if (!spikesToRemove.isEmpty()) {
            centerLoc = spikesToRemove.iterator().next();
        }

        if (centerLoc == null) return;

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            private final List<Location> blockLocations = new ArrayList<>(blocksToRestore.keySet());
            private final List<Location> spikeLocations = new ArrayList<>(spikesToRemove);
            private int blockIndex = 0;
            private int spikeIndex = 0;
            private long lastExecutionTime = 0;
            private int dynamicBatchSize = 60;
            private final int MIN_BATCH_SIZE = 15;
            private final int MAX_BATCH_SIZE = 120;

            @Override
            public void run() {
                if (taskCompleted[0]) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                long startTime = System.nanoTime();
                int processed = 0;
                int targetBatchSize = calculateDynamicBatchSize();

                processed += restoreSnowBlocks(blocksToRestore, targetBatchSize / 2);
                processed += removeIceSpikes(spikesToRemove, targetBatchSize - processed);

                long executionTime = System.nanoTime() - startTime;
                updatePerformanceMetrics(executionTime, processed);

                if (blockIndex >= blockLocations.size() && spikeIndex >= spikeLocations.size()) {
                    completeRestoration(holder);
                    taskCompleted[0] = true;
                    SchedulerWrapper.safeCancelTask(this);
                }
            }

            private int restoreSnowBlocks(Map<Location, Material> blocksToRestore, int maxProcessing) {
                int processed = 0;
                while (blockIndex < blockLocations.size() && processed < maxProcessing) {
                    Location loc = blockLocations.get(blockIndex);
                    Material originalMat = blocksToRestore.get(loc);

                    if (originalMat != null) {
                        try {
                            Block block = loc.getBlock();
                            if (block != null && block.getType() == Material.SNOW_BLOCK) {
                                WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                                    block.setType(originalMat);
                                });
                            }
                            TempBlockStorage.removeTempBlock(loc);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to restore snow block at " + loc + ": " + e.getMessage());
                        }
                    }

                    blockIndex++;
                    processed++;
                }
                return processed;
            }

            private int removeIceSpikes(Set<Location> spikesToRemove, int maxProcessing) {
                int processed = 0;
                List<Location> spikeList = new ArrayList<>(spikesToRemove);

                while (spikeIndex < spikeList.size() && processed < maxProcessing) {
                    Location loc = spikeList.get(spikeIndex);

                    try {
                        Block block = loc.getBlock();
                        if (block != null) {
                            Material currentType = block.getType();
                            if (currentType == Material.PACKED_ICE || currentType == Material.BLUE_ICE) {
                                WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                                    block.setType(Material.AIR);
                                });
                            }
                        }
                        TempBlockStorage.removeTempBlock(loc);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to remove ice spike at " + loc + ": " + e.getMessage());
                    }

                    spikeIndex++;
                    processed++;
                }
                return processed;
            }

            private int calculateDynamicBatchSize() {
                if (lastExecutionTime < 15_000_000) {
                    dynamicBatchSize = Math.min(MAX_BATCH_SIZE, dynamicBatchSize + 10);
                } else if (lastExecutionTime > 45_000_000) {
                    dynamicBatchSize = Math.max(MIN_BATCH_SIZE, dynamicBatchSize - 20);
                }
                return dynamicBatchSize;
            }

            private void updatePerformanceMetrics(long executionTime, int processed) {
                lastExecutionTime = executionTime;

                if (executionTime > 50_000_000) {
                    dynamicBatchSize = Math.max(MIN_BATCH_SIZE, dynamicBatchSize / 2);
                }
            }

            private void completeRestoration(BlockStateHolder holder) {
                holder.clear();
                SchedulerWrapper.runTaskAsynchronously(plugin, TempBlockStorage::flush);
            }
        }, centerLoc, 0L, 1L);
    }
}