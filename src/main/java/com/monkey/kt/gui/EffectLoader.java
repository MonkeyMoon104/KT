package com.monkey.kt.gui;

import com.monkey.kt.KT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EffectLoader {

    private final KT plugin;
    private final List<Material> fallbackMaterials = Arrays.asList(
            Material.STONE, Material.DIRT, Material.GRASS_BLOCK,
            Material.OAK_PLANKS, Material.COBBLESTONE, Material.SAND,
            Material.GLASS, Material.IRON_INGOT, Material.GOLD_INGOT,
            Material.DIAMOND, Material.COAL
    );

    public EffectLoader(KT plugin) {
        this.plugin = plugin;
    }

    public Map<String, ItemStack> loadEffects(Set<String> keys) {
        Map<String, ItemStack> effects = new LinkedHashMap<>();
        List<String> sortedKeys = new ArrayList<>(keys);
        Collections.sort(sortedKeys);

        for (String key : sortedKeys) {
            String matName = EffectIconMap.ICONS.getOrDefault(key, "STONE");
            Material material = Material.matchMaterial(matName);

            if (material == null) {
                int idx = ThreadLocalRandom.current().nextInt(fallbackMaterials.size());
                material = fallbackMaterials.get(idx);
                Bukkit.getLogger().warning("[KT] Material for effect '" + key +
                        "' not found, using fallback: " + material.name());
            }

            String name = plugin.getConfig().getString("effects." + key + ".name");
            if (name == null) name = capitalize(key);

            Object descObj = plugin.getConfig().get("effects." + key + ".description");
            List<String> lore = new ArrayList<>();

            if (descObj instanceof List<?>) {
                for (Object line : (List<?>) descObj) {
                    if (line instanceof String) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', (String) line));
                    }
                }
            } else if (descObj instanceof String) {
                lore.add(ChatColor.translateAlternateColorCodes('&', (String) descObj));
            } else {
                lore.add(capitalize(key));
            }

            effects.put(key, EffectItemBuilder.buildItem(material, name, lore));
        }

        return effects;
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
