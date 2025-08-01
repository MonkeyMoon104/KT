package com.monkey.kt.gui;

import com.monkey.kt.KT;
import com.monkey.kt.economy.KillCoinsEco;
import com.monkey.kt.storage.EffectStorage;
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
    private final KillCoinsEco economy;
    private final EffectLoader loader;

    private final Map<String, ItemStack> effects = new LinkedHashMap<>();

    public GUIManager(KT plugin, KillCoinsEco economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.loader = new EffectLoader(plugin);

        reloadGUI(EffectIconMap.ICONS.keySet());
    }

    public void openGUI(Player player) {
        boolean ecoEnabled = economy.isEnabled();
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui_title"));

        int size = 54;
        Inventory inv = Bukkit.createInventory(null, size, title);

        String alreadyBoughtMsg = plugin.getConfig().getString("messages.already_bought");
        String priceFormat = plugin.getConfig().getString("messages.price_format");
        String currencySym = economy.currencySymbol();

        int maxEffectSlots = 45;
        int slot = 0;
        for (Map.Entry<String, ItemStack> entry : effects.entrySet()) {
            if (slot >= maxEffectSlots) break;
            String key = entry.getKey();
            ItemStack item = entry.getValue().clone();

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

        int leftSlot = 48;
        int centerSlot = 49;
        int rightSlot = 50;

        ItemStack closeItem = new ItemStack(Material.GRAY_WOOL);
        ItemMeta closeMeta = closeItem.getItemMeta();
        String closeName = plugin.getConfig().getString("gui.buttons.close", "&c✖ Close");
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', closeName));
        closeItem.setItemMeta(closeMeta);
        inv.setItem(leftSlot, closeItem);

        String currentEffect = EffectStorage.getEffect(player);
        String effectDisplay = currentEffect != null ? capitalize(currentEffect) : plugin.getConfig().getString("gui.none", "None");

        ItemStack currentEffectItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta currentMeta = currentEffectItem.getItemMeta();
        String currentEffectName = plugin.getConfig().getString("gui.buttons.current_effect", "&eCurrent Effect: %effect%");
        currentEffectName = currentEffectName.replace("%effect%", effectDisplay);
        currentMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', currentEffectName));
        currentEffectItem.setItemMeta(currentMeta);
        inv.setItem(centerSlot, currentEffectItem);

        ItemStack disableItem = new ItemStack(Material.RED_WOOL);
        ItemMeta disableMeta = disableItem.getItemMeta();
        String disableName = plugin.getConfig().getString("gui.buttons.disable", "&4➤ Disable Effect");
        disableMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', disableName));
        disableItem.setItemMeta(disableMeta);
        inv.setItem(rightSlot, disableItem);

        player.openInventory(inv);
    }

    public String getEffectByItem(ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return null;
        String name = clicked.getItemMeta().getDisplayName();
        for (Map.Entry<String, ItemStack> entry : effects.entrySet()) {
            if (entry.getValue().getItemMeta().getDisplayName().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void reloadGUI(Set<String> enabledEffects) {
        effects.clear();
        effects.putAll(loader.loadEffects(enabledEffects));
    }

    public Map<String, ItemStack> getEffects() {
        return effects;
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
