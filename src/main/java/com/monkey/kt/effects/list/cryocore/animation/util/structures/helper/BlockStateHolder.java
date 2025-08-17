package com.monkey.kt.effects.list.cryocore.animation.util.structures.helper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockStateHolder {
    public final Map<Location, Material> originalBlocks = new HashMap<>();
    public final Set<Location> iceSpikeBlocks = new HashSet<>();
    private final Map<Location, Block> blockCache = new HashMap<>();
    private final Set<Location> sensitiveCached = new HashSet<>();

    public Block getCachedBlock(Location location) {
        return blockCache.computeIfAbsent(location, Location::getBlock);
    }

    public boolean isSensitiveCached(Location location) {
        return sensitiveCached.contains(location);
    }

    public void addSensitiveCached(Location location) {
        sensitiveCached.add(location);
    }

    public void clear() {
        originalBlocks.clear();
        iceSpikeBlocks.clear();
        blockCache.clear();
        sensitiveCached.clear();
    }

    public boolean isEmpty() {
        return originalBlocks.isEmpty() && iceSpikeBlocks.isEmpty();
    }

    public int getTotalBlocks() {
        return originalBlocks.size() + iceSpikeBlocks.size();
    }
}