package com.monkey.kt.gui;

import com.monkey.kt.KT;
import org.bukkit.Bukkit;
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
            String canonicalKey = plugin.resolveEffectId(key);
            if (plugin.getCustomEffectLoader() != null) {
                com.monkey.kt.effects.custom.CustomEffectConfig customConfig =
                        plugin.getCustomEffectLoader().getEffectConfig(canonicalKey);

                if (customConfig != null) {
                    Material material = customConfig.getIcon();
                    String name = customConfig.getName();
                    effects.put(canonicalKey, EffectItemBuilder.buildItem(material, name, new ArrayList<>(customConfig.getDescription())));
                    continue;
                }
            }

            String configKey = plugin.getEffectConfigKey(canonicalKey);
            String matName = plugin.getConfig().getString("effects." + configKey + ".icon",
                    EffectIconMap.ICONS.getOrDefault(configKey, "STONE"));
            Material material = Material.matchMaterial(matName);

            if (material == null) {
                int idx = ThreadLocalRandom.current().nextInt(fallbackMaterials.size());
                material = fallbackMaterials.get(idx);
                Bukkit.getLogger().warning("[KT] Material for effect '" + canonicalKey +
                        "' not found, using fallback: " + material.name());
            }

            String name = plugin.getConfig().getString("effects." + configKey + ".name");
            if (name == null) name = capitalize(canonicalKey);

            Object descObj = plugin.getConfig().get("effects." + configKey + ".description");
            List<String> lore = new ArrayList<>();

            if (descObj instanceof List<?>) {
                for (Object line : (List<?>) descObj) {
                    if (line instanceof String) {
                        lore.add((String) line);
                    }
                }
            } else if (descObj instanceof String) {
                lore.add((String) descObj);
            } else {
                lore.add(capitalize(canonicalKey));
            }

            effects.put(canonicalKey, EffectItemBuilder.buildItem(material, name, lore));
        }

        return effects;
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
