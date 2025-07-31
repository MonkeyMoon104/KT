package com.monkey.kt.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SensitiveBlockUtils {

    private static final Set<Material> SENSITIVE_BLOCKS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            Material.REDSTONE_WIRE,
            Material.REPEATER,
            Material.COMPARATOR,
            Material.OBSERVER,
            Material.LEVER,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.STONE_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Material.TARGET,
            Material.TRIPWIRE,
            Material.TRIPWIRE_HOOK,
            Material.REDSTONE_TORCH,
            Material.REDSTONE_BLOCK,

            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BARREL,
            Material.HOPPER,
            Material.DISPENSER,
            Material.DROPPER,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.CRAFTING_TABLE,
            Material.ENCHANTING_TABLE,
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.ENDER_CHEST,
            Material.BREWING_STAND,
            Material.BELL,
            Material.BEACON,
            Material.LODESTONE,
            Material.JUKEBOX,

            Material.RAIL,
            Material.POWERED_RAIL,
            Material.DETECTOR_RAIL,
            Material.ACTIVATOR_RAIL,

            Material.TORCHFLOWER,
            Material.PITCHER_PLANT,
            Material.TORCHFLOWER_CROP,
            Material.PITCHER_CROP,
            Material.SWEET_BERRY_BUSH,
            Material.BEETROOTS,
            Material.CARROTS,
            Material.POTATOES,
            Material.WHEAT,
            Material.COCOA,
            Material.NETHER_WART,
            Material.SUGAR_CANE,
            Material.CACTUS,
            Material.BAMBOO,
            Material.KELP,
            Material.SEAGRASS,
            Material.TALL_SEAGRASS,
            Material.SEA_PICKLE,

            Material.GRASS,
            Material.FERN,
            Material.DEAD_BUSH,
            Material.LARGE_FERN,
            Material.TALL_GRASS,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.SPORE_BLOSSOM,
            Material.TORCH,
            Material.WALL_TORCH,
            Material.SOUL_TORCH,
            Material.SOUL_WALL_TORCH,
            Material.LANTERN,
            Material.SOUL_LANTERN,
            Material.FLOWER_POT,
            Material.SNOW,
            Material.SCAFFOLDING,
            Material.STRING,

            Material.STONE_BUTTON,
            Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON,
            Material.BIRCH_BUTTON,
            Material.JUNGLE_BUTTON,
            Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON,
            Material.MANGROVE_BUTTON,
            Material.CHERRY_BUTTON,
            Material.BAMBOO_BUTTON,
            Material.CRIMSON_BUTTON,
            Material.WARPED_BUTTON,

            Material.OAK_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.MANGROVE_PRESSURE_PLATE,
            Material.CHERRY_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE,
            Material.CRIMSON_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE,

            Material.RED_CARPET,
            Material.BLACK_CARPET,
            Material.BLUE_CARPET,
            Material.BROWN_CARPET,
            Material.CYAN_CARPET,
            Material.GRAY_CARPET,
            Material.GREEN_CARPET,
            Material.LIGHT_BLUE_CARPET,
            Material.LIGHT_GRAY_CARPET,
            Material.LIME_CARPET,
            Material.MAGENTA_CARPET,
            Material.ORANGE_CARPET,
            Material.PINK_CARPET,
            Material.PURPLE_CARPET,
            Material.WHITE_CARPET,
            Material.YELLOW_CARPET
    )));

    public static boolean isSensitive(Block block) {
        return SENSITIVE_BLOCKS.contains(block.getType());
    }

    public static boolean isSensitive(Material material) {
        return SENSITIVE_BLOCKS.contains(material);
    }
}
