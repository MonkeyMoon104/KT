package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.effects.permission.EffectPermissionResolver;
import com.monkey.kt.gui.layout.GuiLayoutConfig;
import com.monkey.kt.storage.EffectStorage;
import com.monkey.kt.utils.text.TextUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final KT plugin;
    private EconomyManager eco;
    private final LuckPerms luckPerms;

    public InventoryClickListener(KT plugin, EconomyManager eco) {
        this.plugin = plugin;
        this.eco = eco;
        this.luckPerms = LuckPermsProvider.get();
    }

    public void updateEconomyManager(EconomyManager economyManager) {
        this.eco = economyManager;
        plugin.getLogger().info("InventoryClickListener updated with new EconomyManager: " +
                (economyManager.isUsingInternal() ? "Internal" : "External"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String guiTitle = plugin.getConfig().getString("messages.gui_title");

        if (!event.getView().title().equals(TextUtils.component(guiTitle))) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= topSize) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();

        GuiLayoutConfig layout = plugin.getGuiManager().getLayoutConfig();

        if (rawSlot == layout.getCloseButton().getSlot()) {
            player.closeInventory();
            return;
        }

        if (rawSlot == layout.getDisableButton().getSlot()) {
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

        if (rawSlot == layout.getCurrentEffectButton().getSlot() || rawSlot == layout.getCurrencyButton().getSlot()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        String effect = plugin.getGuiManager().getEffectByItem(clicked);
        if (effect == null) {
            return;
        }

        String permission = EffectPermissionResolver.resolvePermission(plugin, effect);
        boolean hasPermission = EffectPermissionResolver.hasPermission(player, plugin, effect);
        boolean explicitPermissionRule = EffectPermissionResolver.hasExplicitPermissionRule(plugin, effect);

        if (!player.isOp() && explicitPermissionRule && !hasPermission) {
            player.sendMessage(color(plugin.getConfig().getString("messages.no_permissions")));
            return;
        }

        if (eco.isEnabled()) {
            if (!eco.hasBoughtEffect(player, effect)) {
                double price;

                if (plugin.getCustomEffectLoader() != null) {
                    com.monkey.kt.effects.custom.CustomEffectConfig customConfig =
                            plugin.getCustomEffectLoader().getEffectConfig(effect);

                    if (customConfig != null) {
                        price = customConfig.getPrice();
                    } else {
                        price = eco.getEffectPrice(effect);
                    }
                } else {
                    price = eco.getEffectPrice(effect);
                }

                if (!eco.has(player, price)) {
                    player.sendMessage(color(plugin.getConfig().getString("messages.not_enough_coins")));
                    return;
                }

                if (!eco.tryBuyEffect(player, effect)) {
                    player.sendMessage(color(plugin.getConfig().getString("messages.purchase_failed")));
                    return;
                }

                giveLuckPermsPermission(player, permission);

                player.sendMessage(color(plugin.getConfig().getString("messages.purchase_success")
                        .replace("%effect%", effect)));
            }
        } else if (!hasPermission) {
            player.sendMessage(color(plugin.getConfig().getString("messages.no_permissions")));
            return;
        }

        if (!hasPermission) {
            if (!eco.isEnabled() || !eco.hasBoughtEffect(player, effect)) {
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

    private void giveLuckPermsPermission(Player player, String permission) {
        if (permission == null || permission.trim().isEmpty() || permission.equalsIgnoreCase("none")) {
            return;
        }

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            user.data().add(PermissionNode.builder(permission).build());
            luckPerms.getUserManager().saveUser(user);
        } else {
            luckPerms.getUserManager().loadUser(player.getUniqueId()).thenAccept(loadedUser -> {
                loadedUser.data().add(PermissionNode.builder(permission).build());
                luckPerms.getUserManager().saveUser(loadedUser);
            });
        }
    }

    private String color(String s) {
        return TextUtils.legacySection(s);
    }
}
