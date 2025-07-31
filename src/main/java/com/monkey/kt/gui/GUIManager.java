package com.monkey.kt.gui;

import com.monkey.kt.KT;
import com.monkey.kt.economy.KillCoinsEco;
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
    private final KillCoinsEco economy;

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
        effectIconNames.put("wither", "WITHER_SKELETON_SKULL");
        effectIconNames.put("stellarcollapse", "NETHER_STAR");
        effectIconNames.put("dimensionalrift", "CRYING_OBSIDIAN");
        effectIconNames.put("cryocore", "SNOW");
    }

    public GUIManager(KT plugin, DatabaseManager databaseManager, KillCoinsEco economy) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.economy = economy;

        List<Material> fallbackMaterials = Arrays.asList(
                Material.STONE, Material.DIRT, Material.GRASS_BLOCK,
                Material.OAK_PLANKS, Material.COBBLESTONE, Material.SAND,
                Material.GLASS, Material.IRON_INGOT, Material.GOLD_INGOT,
                Material.DIAMOND, Material.COAL
        );

        List<String> keys = new ArrayList<>(effectIconNames.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            String matName = effectIconNames.get(key);
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

            effects.put(key, createEffectItem(
                    material,
                    ChatColor.translateAlternateColorCodes('&', name),
                    lore
            ));
        }
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
        boolean ecoEnabled = economy.isEnabled();
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui_title"));
        int size = ((effects.size() - 1) / 9 + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, title);

        String alreadyBoughtMsg = plugin.getConfig().getString("messages.already_bought");
        String priceFormat     = plugin.getConfig().getString("messages.price_format");
        String currencySym     = economy.currencySymbol();

        int slot = 0;
        for (Map.Entry<String, ItemStack> entry : effects.entrySet()) {
            String key      = entry.getKey();
            ItemStack item  = entry.getValue().clone();

            List<String> lore = new ArrayList<>(item.getItemMeta().getLore());
            if (ecoEnabled) {
                if (economy.hasBoughtEffect(player, key)) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', alreadyBoughtMsg));
                } else {
                    double price = economy.getEffectPrice(key);
                    String line = priceFormat
                            .replace("%price%", String.valueOf((int) price))
                            .replace("%currency%", currencySym);
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }

            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }

    public String getEffectByItem(ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return null;
        String name = clicked.getItemMeta().getDisplayName();
        for (Map.Entry<String, ItemStack> entry : effects.entrySet()) {
            ItemMeta meta = entry.getValue().getItemMeta();
            if (meta != null && name.equals(meta.getDisplayName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void reloadGUI(Set<String> enabledEffects) {
        effects.clear();
        List<String> keys = new ArrayList<>(enabledEffects);
        Collections.sort(keys);
        for (String key : keys) {
            Material material = Material.matchMaterial(
                    effectIconNames.getOrDefault(key, "STONE")
            );
            if (material == null) material = Material.STONE;

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

            effects.put(key, createEffectItem(
                    material,
                    ChatColor.translateAlternateColorCodes('&', name),
                    lore
            ));
        }
    }

    public Map<String, ItemStack> getEffects() {
        return effects;
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
