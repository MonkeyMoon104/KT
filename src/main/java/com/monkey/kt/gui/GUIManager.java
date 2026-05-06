package com.monkey.kt.gui;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.gui.layout.GuiLayoutConfig;
import com.monkey.kt.storage.EffectStorage;
import com.monkey.kt.utils.text.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GUIManager {

    private final KT plugin;
    private EconomyManager eco;
    private final EffectLoader loader;

    private final Map<String, ItemStack> effects = new LinkedHashMap<>();
    private GuiLayoutConfig layoutConfig;

    public GUIManager(KT plugin, EconomyManager eco) {
        this(plugin, eco, EffectIconMap.ICONS.keySet());
    }

    public GUIManager(KT plugin, EconomyManager eco, Set<String> enabledEffects) {
        this.plugin = plugin;
        this.eco = eco;
        this.loader = new EffectLoader(plugin);

        plugin.getLogger().info("GUIManager constructor - received " + enabledEffects.size() + " effects");
        plugin.getLogger().info("Effects list: " + String.join(", ", enabledEffects));

        reloadGUI(enabledEffects);

        plugin.getLogger().info("GUIManager constructor - loaded " + effects.size() + " effects in GUI");
    }

    public void updateEconomyManager(EconomyManager economyManager) {
        this.eco = economyManager;
        plugin.getLogger().info("GUIManager updated with new EconomyManager: " +
                (economyManager.isUsingInternal() ? "Internal" : "External"));
    }

    public void openGUI(Player player) {
        boolean ecoEnabled = eco.isEnabled();
        String title = plugin.getConfig().getString("messages.gui_title");

        layoutConfig = GuiLayoutConfig.fromConfig(plugin.getConfig());
        Inventory inv = Bukkit.createInventory(null, layoutConfig.getSize(), TextUtils.component(title));

        String alreadyBoughtMsg = plugin.getConfig().getString("messages.already_bought");
        String priceFormat = plugin.getConfig().getString("messages.price_format");
        String currencySym = eco.currencySymbol();

        List<Integer> availableSlots = layoutConfig.getEffectSlots();
        int slotIndex = 0;
        for (Map.Entry<String, ItemStack> entry : effects.entrySet()) {
            if (slotIndex >= availableSlots.size()) {
                break;
            }

            String key = entry.getKey();
            ItemStack item = entry.getValue().clone();

            List<Component> lore = new ArrayList<>();
            ItemMeta sourceMeta = item.getItemMeta();
            if (sourceMeta != null && sourceMeta.lore() != null) {
                lore.addAll(sourceMeta.lore());
            }
            if (ecoEnabled) {
                boolean isCustom = plugin.getCustomEffectLoader() != null &&
                        plugin.getCustomEffectLoader().getEffectConfig(key) != null;

                if (isCustom) {
                    com.monkey.kt.effects.custom.CustomEffectConfig customConfig =
                            plugin.getCustomEffectLoader().getEffectConfig(key);

                    if (eco.hasBoughtEffect(player, key)) {
                        lore.add(TextUtils.component(alreadyBoughtMsg));
                    } else {
                        double price = customConfig.getPrice();
                        String line = priceFormat
                                .replace("%price%", String.valueOf((int) price))
                                .replace("%currency%", currencySym);
                        lore.add(TextUtils.component(line));
                    }
                } else {
                    if (eco.hasBoughtEffect(player, key)) {
                        lore.add(TextUtils.component(alreadyBoughtMsg));
                    } else {
                        double price = eco.getEffectPrice(key);
                        String line = priceFormat
                                .replace("%price%", String.valueOf((int) price))
                                .replace("%currency%", currencySym);
                        lore.add(TextUtils.component(line));
                    }
                }
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.lore(lore);
                item.setItemMeta(meta);
            }

            inv.setItem(availableSlots.get(slotIndex++), item);
        }

        ItemStack closeItem = new ItemStack(layoutConfig.getCloseButton().getMaterial());
        ItemMeta closeMeta = closeItem.getItemMeta();
        String closeName = plugin.getConfig().getString("gui.buttons.close", "&cX Close");
        if (closeMeta != null) {
            closeMeta.displayName(TextUtils.component(closeName));
            closeItem.setItemMeta(closeMeta);
        }
        inv.setItem(layoutConfig.getCloseButton().getSlot(), closeItem);

        String currentEffect = EffectStorage.getEffect(player);
        String effectDisplay = currentEffect != null ? capitalize(currentEffect) : plugin.getConfig().getString("gui.none", "None");

        ItemStack currentEffectItem = new ItemStack(layoutConfig.getCurrentEffectButton().getMaterial());
        ItemMeta currentMeta = currentEffectItem.getItemMeta();
        String currentEffectName = plugin.getConfig().getString("gui.buttons.current_effect", "&eCurrent Effect: %effect%");
        currentEffectName = currentEffectName.replace("%effect%", effectDisplay);
        if (currentMeta != null) {
            currentMeta.displayName(TextUtils.component(currentEffectName));
            currentEffectItem.setItemMeta(currentMeta);
        }
        inv.setItem(layoutConfig.getCurrentEffectButton().getSlot(), currentEffectItem);

        ItemStack disableItem = new ItemStack(layoutConfig.getDisableButton().getMaterial());
        ItemMeta disableMeta = disableItem.getItemMeta();
        String disableName = plugin.getConfig().getString("gui.buttons.disable", "&4> Disable Effect");
        if (disableMeta != null) {
            disableMeta.displayName(TextUtils.component(disableName));
            disableItem.setItemMeta(disableMeta);
        }
        inv.setItem(layoutConfig.getDisableButton().getSlot(), disableItem);

        ItemStack currencyItem = new ItemStack(layoutConfig.getCurrencyButton().getMaterial());
        ItemMeta currencyMeta = currencyItem.getItemMeta();
        String currencyName = plugin.getConfig().getString("gui.buttons.currency", "&7Your balance: &a%bal% %bal_symbol%");
        currencyName = currencyName
                .replace("%bal%", String.valueOf(eco.getBalance(player)))
                .replace("%bal_symbol%", eco.currencySymbol());
        if (currencyMeta != null) {
            currencyMeta.displayName(TextUtils.component(currencyName));
            currencyItem.setItemMeta(currencyMeta);
        }
        inv.setItem(layoutConfig.getCurrencyButton().getSlot(), currencyItem);

        player.openInventory(inv);
    }

    public String getEffectByItem(ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) {
            return null;
        }

        ItemMeta clickedMeta = clicked.getItemMeta();
        if (clickedMeta == null || clickedMeta.displayName() == null) {
            return null;
        }

        Component name = clickedMeta.displayName();
        for (Map.Entry<String, ItemStack> entry : effects.entrySet()) {
            ItemMeta effectMeta = entry.getValue().getItemMeta();
            if (effectMeta != null && name.equals(effectMeta.displayName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void reloadGUI(Set<String> enabledEffects) {
        effects.clear();
        layoutConfig = GuiLayoutConfig.fromConfig(plugin.getConfig());

        plugin.getLogger().info("Reloading GUI with " + enabledEffects.size() + " enabled effects");
        plugin.getLogger().info("Registered effects in factory: " +
                com.monkey.kt.effects.KillEffectFactory.getRegisteredEffects().size());

        effects.putAll(loader.loadEffects(enabledEffects));

        plugin.getLogger().info("GUI reloaded with " + effects.size() + " effects");

        if (!effects.isEmpty()) {
            plugin.getLogger().info("Loaded effects: " + String.join(", ", effects.keySet()));
        }
    }

    public Map<String, ItemStack> getEffects() {
        return effects;
    }

    public EconomyManager getEconomyManager() {
        return eco;
    }

    public GuiLayoutConfig getLayoutConfig() {
        if (layoutConfig == null) {
            layoutConfig = GuiLayoutConfig.fromConfig(plugin.getConfig());
        }
        return layoutConfig;
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
