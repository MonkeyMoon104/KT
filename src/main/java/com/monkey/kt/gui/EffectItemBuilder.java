package com.monkey.kt.gui;

import com.monkey.kt.utils.text.TextUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EffectItemBuilder {

    public static ItemStack buildItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtils.component(name));
            meta.lore(TextUtils.components(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
