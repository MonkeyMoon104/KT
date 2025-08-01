package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.economy.KillCoinsEco;
import com.monkey.kt.storage.EffectStorage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final KT plugin;
    private final KillCoinsEco economy;
    private final LuckPerms luckPerms;

    public InventoryClickListener(KT plugin, KillCoinsEco killCoinsEco) {
        this.plugin = plugin;
        this.economy = killCoinsEco;
        this.luckPerms = LuckPermsProvider.get();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String guiTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui_title"));

        if (!event.getView().getTitle().equals(guiTitle)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        String closeName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.buttons.close", "&c✖ Close")));
        if (displayName.equalsIgnoreCase(closeName)) {
            player.closeInventory();
            return;
        }

        String disableName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.buttons.disable", "&4➤ Disable Effect")));
        if (displayName.equalsIgnoreCase(disableName)) {
            String current = EffectStorage.getEffect(player);
            if (current == null) {
                player.sendMessage(color(plugin.getConfig().getString("messages.effect_none_selected")));
                return;
            }
            EffectStorage.removeEffect(player);
            player.sendMessage(color(plugin.getConfig().getString("messages.effect_removed")));
            player.closeInventory();
            return;
        }

        String currentRaw = plugin.getConfig().getString("gui.buttons.current_effect", "&eCurrent Effect: %effect%");
        String currentEffectPrefix = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                currentRaw.split("%effect%")[0]));
        if (displayName.startsWith(currentEffectPrefix)) {
            return;
        }

        String effect = plugin.getGuiManager().getEffectByItem(clicked);
        if (effect == null) return;

        String perm = "kt." + effect + ".use";

        if (economy.isEnabled()) {
            if (!economy.hasBoughtEffect(player, effect)) {
                double price = economy.getEffectPrice(effect);
                if (!economy.has(player, price)) {
                    player.sendMessage(color(plugin.getConfig().getString("messages.not_enough_coins")));
                    return;
                }
                if (!economy.tryBuyEffect(player, effect)) {
                    player.sendMessage(color(plugin.getConfig().getString("messages.purchase_failed")));
                    return;
                }

                giveLuckPermsPermission(player, perm);

                player.sendMessage(color(plugin.getConfig().getString("messages.purchase_success")
                        .replace("%effect%", effect)));
            }
        } else {
            if (!player.hasPermission(perm)) {
                player.sendMessage(color(plugin.getConfig().getString("messages.no_permissions")));
                return;
            }
        }

        if (!player.hasPermission(perm)) {
            if (!economy.isEnabled() || !economy.hasBoughtEffect(player, effect)) {
                player.sendMessage(color(plugin.getConfig().getString("messages.no_permissions")));
                return;
            }
        }

        String current = EffectStorage.getEffect(player);
        if (effect.equalsIgnoreCase(current)) {
            player.sendMessage(color(plugin.getConfig().getString("messages.effect_already_set")));
            return;
        }

        EffectStorage.setEffect(player, effect);
        player.sendMessage(color(plugin.getConfig().getString("messages.effect_set")
                .replace("%effect%", effect)));

        player.closeInventory();
    }

    private void giveLuckPermsPermission(Player player, String perm) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            user.data().add(PermissionNode.builder(perm).build());
            luckPerms.getUserManager().saveUser(user);
        } else {
            luckPerms.getUserManager().loadUser(player.getUniqueId()).thenAccept(loadedUser -> {
                loadedUser.data().add(PermissionNode.builder(perm).build());
                luckPerms.getUserManager().saveUser(loadedUser);
            });
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}