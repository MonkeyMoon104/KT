package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.storage.EffectStorage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final KT plugin;

    public InventoryClickListener(KT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String guiTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_title"));

        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();


            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String effect = plugin.getGuiManager().getEffectByItem(clicked);
            if (effect == null) return;

            if (!player.hasPermission("kt." + effect + ".use")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no_permissions")));
                return;
            }

            String current = EffectStorage.getEffect(player);
            if (effect.equalsIgnoreCase(current)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.effect_already_set")));
                return;
            }

            EffectStorage.setEffect(player, effect);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.effect_set").replace("%effect%", effect)));

            player.closeInventory();
        }
    }
}
