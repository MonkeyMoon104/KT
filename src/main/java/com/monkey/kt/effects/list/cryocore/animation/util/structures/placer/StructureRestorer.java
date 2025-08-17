package com.monkey.kt.effects.list.cryocore.animation.util.structures.placer;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.helper.BlockStateHolder;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StructureRestorer {

    private final KT plugin;

    public StructureRestorer(KT plugin) {
        this.plugin = plugin;
    }

    public void restoreGround(BlockStateHolder holder) {
        final Map<Location, Material> blocksToRestore = new HashMap<>(holder.originalBlocks);
        final Set<Location> spikesToRemove = new HashSet<>(holder.iceSpikeBlocks);

        final Map<Location, Block> cachedBlocks = new HashMap<>();
        final Map<Location, Material> cachedMaterials = new HashMap<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            cacheBlocksForRestoration(blocksToRestore, spikesToRemove, cachedBlocks, cachedMaterials);

            Bukkit.getScheduler().runTask(plugin, () -> {
                startOptimizedRestoration(holder, blocksToRestore, spikesToRemove, cachedBlocks, cachedMaterials);
            });
        });
    }

    private void cacheBlocksForRestoration(Map<Location, Material> blocksToRestore,
                                           Set<Location> spikesToRemove,
                                           Map<Location, Block> cachedBlocks,
                                           Map<Location, Material> cachedMaterials) {
        for (Location loc : blocksToRestore.keySet()) {
            Block block = loc.getBlock();
            cachedBlocks.put(loc, block);
            cachedMaterials.put(loc, block.getType());
        }
        for (Location loc : spikesToRemove) {
            Block block = loc.getBlock();
            cachedBlocks.put(loc, block);
            cachedMaterials.put(loc, block.getType());
        }
    }

    private void startOptimizedRestoration(BlockStateHolder holder,
                                           Map<Location, Material> blocksToRestore,
                                           Set<Location> spikesToRemove,
                                           Map<Location, Block> cachedBlocks,
                                           Map<Location, Material> cachedMaterials) {

        new BukkitRunnable() {
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
                long startTime = System.nanoTime();
                int processed = 0;
                int targetBatchSize = calculateDynamicBatchSize();

                processed += restoreSnowBlocks(blocksToRestore, cachedBlocks, cachedMaterials, targetBatchSize / 2);

                processed += removeIceSpikes(spikesToRemove, cachedBlocks, cachedMaterials, targetBatchSize - processed);

                long executionTime = System.nanoTime() - startTime;
                updatePerformanceMetrics(executionTime, processed);

                if (blockIndex >= blockLocations.size() && spikeIndex >= spikeLocations.size()) {
                    completeRestoration(holder);
                    cancel();
                }
            }

            private int restoreSnowBlocks(Map<Location, Material> blocksToRestore,
                                          Map<Location, Block> cachedBlocks,
                                          Map<Location, Material> cachedMaterials,
                                          int maxProcessing) {
                int processed = 0;
                while (blockIndex < blockLocations.size() && processed < maxProcessing) {
                    Location loc = blockLocations.get(blockIndex);
                    Material originalMat = blocksToRestore.get(loc);

                    if (originalMat != null) {
                        Block cachedBlock = cachedBlocks.get(loc);
                        Material currentType = cachedMaterials.get(loc);

                        if (currentType == Material.SNOW_BLOCK) {
                            WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                                cachedBlock.setType(originalMat);
                            });
                        }

                        TempBlockStorage.removeTempBlock(loc);
                    }

                    blockIndex++;
                    processed++;
                }
                return processed;
            }

            private int removeIceSpikes(Set<Location> spikesToRemove,
                                        Map<Location, Block> cachedBlocks,
                                        Map<Location, Material> cachedMaterials,
                                        int maxProcessing) {
                int processed = 0;
                List<Location> spikeList = new ArrayList<>(spikesToRemove);

                while (spikeIndex < spikeList.size() && processed < maxProcessing) {
                    Location loc = spikeList.get(spikeIndex);
                    Block cachedBlock = cachedBlocks.get(loc);
                    Material currentType = cachedMaterials.get(loc);

                    if (currentType == Material.PACKED_ICE || currentType == Material.BLUE_ICE) {
                        WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                            cachedBlock.setType(Material.AIR);
                        });
                    }

                    TempBlockStorage.removeTempBlock(loc);
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
                Bukkit.getScheduler().runTaskAsynchronously(plugin, TempBlockStorage::flush);
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }
}