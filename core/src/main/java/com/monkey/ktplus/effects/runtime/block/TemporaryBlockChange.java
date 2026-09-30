package com.monkey.ktplus.effects.runtime.block;

import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public final class TemporaryBlockChange {
    private final String key;
    private final Block block;
    private final BlockState original;
    private Material expectedMaterial;

    public TemporaryBlockChange(String key, Block block, BlockState original, Material expectedMaterial) {
        this.key = Objects.requireNonNull(key, "key");
        this.block = Objects.requireNonNull(block, "block");
        this.original = Objects.requireNonNull(original, "original");
        this.expectedMaterial = Objects.requireNonNull(expectedMaterial, "expectedMaterial");
    }

    public String key() {
        return key;
    }

    public void restore() {
        original.update(true, false);
    }

    public Block block() {
        return block;
    }

    public Material expectedMaterial() {
        return expectedMaterial;
    }

    public void setExpectedMaterial(Material expectedMaterial) {
        this.expectedMaterial = Objects.requireNonNull(expectedMaterial, "expectedMaterial");
    }
}
