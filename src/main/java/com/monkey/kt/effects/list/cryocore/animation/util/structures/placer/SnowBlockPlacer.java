package com.monkey.kt.effects.list.cryocore.animation.util.structures.placer;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.helper.BlockStateHolder;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.SensitiveBlockUtils;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SnowBlockPlacer {

    private final KT plugin;

    public SnowBlockPlacer(KT plugin) {
        this.plugin = plugin;
    }

    public void randomlyPlaceSnowBlocks(Location center, int radius, BlockStateHolder holder,
                                        int batch, boolean allowStructure) {
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

        for (int i = candidatePositions.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Location temp = candidatePositions.get(i);
            candidatePositions.set(i, candidatePositions.get(j));
            candidatePositions.set(j, temp);
        }

        startSnowPlacement(candidatePositions, holder);
    }

    private void startSnowPlacement(List<Location> candidatePositions, BlockStateHolder holder) {
        new BukkitRunnable() {
            private final List<Location> snowLocations = new ArrayList<>(candidatePositions);
            private int snowindex = 0;

            @Override
            public void run() {
                int processed = 0;
                while (snowindex < snowLocations.size() && processed < 35) {
                    Location surfaceLoc = snowLocations.get(snowindex);
                    Block targetBlock = surfaceLoc.getBlock();
                    Block above = surfaceLoc.clone().add(0, 1, 0).getBlock();

                    if (!SensitiveBlockUtils.isSensitive(targetBlock) &&
                            !SensitiveBlockUtils.isSensitive(above) &&
                            !holder.originalBlocks.containsKey(targetBlock.getLocation())) {

                        Material originalMat = targetBlock.getType();
                        Location loc = targetBlock.getLocation();

                        holder.originalBlocks.put(loc, originalMat);

                        WorldGuardUtils.runWithWorldGuardBypass(loc, () -> {
                            targetBlock.setType(Material.SNOW_BLOCK);
                        });

                        TempBlockStorage.saveTempBlock(loc, originalMat);
                    }

                    snowindex++;
                    processed++;
                }

                if (snowindex >= snowLocations.size()) {
                    TempBlockStorage.flush();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}