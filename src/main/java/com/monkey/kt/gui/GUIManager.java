package com.monkey.kt.gui;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {

    private final KT plugin;
    private final DatabaseManager databaseManager;

    private final Map<String, ItemStack> effects = new LinkedHashMap<>();

    private static final Map<String, Material> effectIcons = new LinkedHashMap<>();
    static {
        effectIcons.put("fire", Material.BLAZE_POWDER);
        effectIcons.put("lightning", Material.LIGHTNING_ROD);
        effectIcons.put("explosion", Material.TNT);
        effectIcons.put("hearts", Material.RED_DYE);
        effectIcons.put("notes", Material.NOTE_BLOCK);
        effectIcons.put("cloud", Material.COBWEB);
        effectIcons.put("smoke", Material.CAMPFIRE);
        effectIcons.put("totem", Material.TOTEM_OF_UNDYING);
        effectIcons.put("end", Material.ENDER_PEARL);
        effectIcons.put("pigstep", Material.PIG_SPAWN_EGG);
        effectIcons.put("warden", Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);
        effectIcons.put("glowmissile", Material.GLOW_INK_SAC);
    }

    public GUIManager(KT plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;

        for (Map.Entry<String, Material> entry : effectIcons.entrySet()) {
            String key = entry.getKey();
            Material material = entry.getValue();

            String name = plugin.getConfig().getString("effects." + key + ".name");
            String description = plugin.getConfig().getString("effects." + key + ".description");

            if (name == null) name = capitalize(key);
            if (description == null) description = key;

            effects.put(key, createEffectItem(material,
                    ChatColor.translateAlternateColorCodes('&', name),
                    ChatColor.translateAlternateColorCodes('&', description)));
        }
    }

    private ItemStack createEffectItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Collections.singletonList(description));
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

    public Map<String, ItemStack> getEffects() {
        return effects;
    }

    private String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
