package com.monkey.kt.gui;

import com.monkey.kt.KT;
import com.monkey.kt.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GUIManager {

    private final KT plugin;
    private final DatabaseManager databaseManager;

    private final Map<String, ItemStack> effects = new LinkedHashMap<>();

    private static final Map<String, String> effectIconNames = new LinkedHashMap<>();
    static {
        effectIconNames.put("cloud", "COBWEB");
        effectIconNames.put("enchantcolumn", "ENCHANTING_TABLE");
        effectIconNames.put("end", "ENDER_PEARL");
        effectIconNames.put("explosion", "TNT");
        effectIconNames.put("fire", "BLAZE_POWDER");
        effectIconNames.put("fireworks", "FIREWORK_ROCKET");
        effectIconNames.put("glowmissile", "GLOW_INK_SAC");
        effectIconNames.put("hearts", "RED_DYE");
        effectIconNames.put("lightning", "LIGHTNING_ROD");
        effectIconNames.put("notes", "NOTE_BLOCK");
        effectIconNames.put("pigstep", "PIG_SPAWN_EGG");
        effectIconNames.put("smoke", "CAMPFIRE");
        effectIconNames.put("sniper", "ARROW");
        effectIconNames.put("totem", "TOTEM_OF_UNDYING");
        effectIconNames.put("warden", "WARD_ARMOR_TRIM_SMITHING_TEMPLATE");
    }

    public GUIManager(KT plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;

        List<Material> fallbackMaterials = Arrays.asList(
                Material.STONE,
                Material.DIRT,
                Material.GRASS_BLOCK,
                Material.OAK_PLANKS,
                Material.COBBLESTONE,
                Material.SAND,
                Material.GLASS,
                Material.IRON_INGOT,
                Material.GOLD_INGOT,
                Material.DIAMOND,
                Material.COAL
        );

        effectIconNames.keySet().stream().sorted().forEach(key -> {
            String matName = effectIconNames.get(key);

            Material material = Material.matchMaterial(matName);
            if (material == null) {
                int randomIndex = ThreadLocalRandom.current().nextInt(fallbackMaterials.size());
                material = fallbackMaterials.get(randomIndex);
                Bukkit.getLogger().warning("[MobAttack] Material for effect '" + key + "' not found, using random fallback: " + material.name());
            }

            String name = plugin.getConfig().getString("effects." + key + ".name");
            Object descObj = plugin.getConfig().get("effects." + key + ".description");

            if (name == null) name = capitalize(key);

            List<String> lore = new ArrayList<>();

            if (descObj instanceof List) {
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

            effects.put(key, createEffectItem(
                    material,
                    ChatColor.translateAlternateColorCodes('&', name),
                    lore
            ));
        });
    }

    private ItemStack createEffectItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void openGUI(Player player) {
        int size = ((effects.size() - 1) / 9 + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui_title")));

        int slot = 0;
        for (ItemStack item : effects.values()) {
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }

    public String getEffectByItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        for (Map.Entry<String, ItemStack> entry : effects.entrySet()) {
            if (item.isSimilar(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void reloadGUI(Set<String> enabledEffects) {
        effects.clear();

        enabledEffects.stream().sorted().forEach(key -> {
            String matName = effectIconNames.getOrDefault(key, "STONE");
            Material material = Material.matchMaterial(matName);
            if (material == null) material = Material.STONE;

            String name = plugin.getConfig().getString("effects." + key + ".name");
            Object descObj = plugin.getConfig().get("effects." + key + ".description");

            if (name == null) name = capitalize(key);
            List<String> lore = new ArrayList<>();

            if (descObj instanceof List) {
                for (Object line : (List<?>) descObj) {
                    if (line instanceof String) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', (String) line));
                    }
                }
            } else if (descObj instanceof String) {
                lore.add(ChatColor.translateAlternateColorCodes('&', (String) descObj));
            }

            effects.put(key, createEffectItem(material,
                    ChatColor.translateAlternateColorCodes('&', name),
                    lore));
        });
    }

    public Map<String, ItemStack> getEffects() {
        return effects;
    }

    private String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
