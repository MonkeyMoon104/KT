package com.monkey.kt.effects.list.grave.animation.util.structure.placer;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.helper.BlockStateHolder;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.SensitiveBlockUtils;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;

import java.util.*;

public class GravePlacer {

    private final KT plugin;

    public GravePlacer(KT plugin) {
        this.plugin = plugin;
    }

    public void randomlyPlaceCoarseDirt(Location center, int radius, BlockStateHolder holder, int batch, boolean allowStructure) {
        if (!allowStructure) return;
        World world = center.getWorld();
        if (world == null) return;

        Random random = new Random(batch);
        int radiusSquared = radius * radius;

        List<Location> candidatePositions = new ArrayList<>();
        int maxBlocks = Math.min(45, radiusSquared / 4);
        Set<String> usedPositions = new HashSet<>();

        for (int i = 0; i < maxBlocks * 3 && candidatePositions.size() < maxBlocks; i++) {
            int x = random.nextInt(radius * 2 + 1) - radius;
            int z = random.nextInt(radius * 2 + 1) - radius;
            if (x * x + z * z > radiusSquared) continue;
            if (random.nextDouble() > 0.65) {
                int baseX = center.getBlockX() + x;
                int baseZ = center.getBlockZ() + z;
                int surfaceY = world.getHighestBlockYAt(baseX, baseZ);
                Location surfaceLoc = new Location(world, baseX, surfaceY, baseZ);
                String key = baseX + "," + surfaceY + "," + baseZ;
                if (!usedPositions.contains(key)) {
                    usedPositions.add(key);
                    candidatePositions.add(surfaceLoc);
                }
            }
        }

        Collections.shuffle(candidatePositions, random);

        if (!candidatePositions.isEmpty()) {
            startCoarseDirtPlacement(candidatePositions, holder);
        } else {
            Bukkit.getLogger().warning("candidatepositions empty!");
        }
    }

    private void startCoarseDirtPlacement(List<Location> candidatePositions, BlockStateHolder holder) {
        final boolean[] taskCompleted = {false};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            private final List<Location> dirtLocations = new ArrayList<>(candidatePositions);
            private int index = 0;

            @Override
            public void run() {
                if (taskCompleted[0]) return;
                int processed = 0;
                while (index < dirtLocations.size() && processed < 3) {
                    Location groundLoc = dirtLocations.get(index);
                    Block targetBlock = groundLoc.getBlock();

                    if (!SensitiveBlockUtils.isSensitive(targetBlock)) {

                        Material originalMat = targetBlock.getType();
                        if (originalMat == Material.DIRT
                                || originalMat == Material.GRASS_BLOCK
                                || originalMat == Material.PODZOL
                                || originalMat == Material.COARSE_DIRT) {

                            Location loc = targetBlock.getLocation();

                            if (!holder.originalBlocks.containsKey(loc)) {
                                holder.originalBlocks.put(loc, originalMat);
                                TempBlockStorage.saveTempBlock(loc, originalMat);
                            }

                            if (originalMat != Material.COARSE_DIRT) {
                                WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                                    targetBlock.setType(Material.COARSE_DIRT);
                                });
                            }

                        } 
                    }
                    index++;
                    processed++;
                }

                if (index >= dirtLocations.size()) {
                    taskCompleted[0] = true;
                    TempBlockStorage.flush();
                    SchedulerWrapper.safeCancelTask(this);
                }
            }
        }, candidatePositions.get(0),0L, 2L);
    }

}