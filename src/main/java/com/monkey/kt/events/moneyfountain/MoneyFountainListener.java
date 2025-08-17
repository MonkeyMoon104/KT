package com.monkey.kt.events.moneyfountain;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import java.util.HashSet;
import java.util.Set;

public class MoneyFountainListener implements Listener {

    private static final Set<Item> fountainItems = new HashSet<>();
    private static MoneyFountainEvent currentEvent;
    private final KT plugin;
    public MoneyFountainListener(KT plugin) {
        this.plugin = plugin;
    }

    public static void registerItem(Item item) {
        fountainItems.add(item);
    }

    public static void removeAllItems() {
        for (Item item : fountainItems) {
            if (item != null && item.isValid()) {
                item.remove();
            }
        }
        fountainItems.clear();
    }

    public static void setCurrentEvent(MoneyFountainEvent event) {
        currentEvent = event;
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        EconomyManager eco = plugin.getEconomyManager();
        Item item = event.getItem();
        if (!fountainItems.contains(item)) return;

        Player player = event.getPlayer();
        double moneyValue = plugin.getConfig().getDouble("events.list.money_fountain.settings.money_value", 20);

        eco.add(player, moneyValue);

        event.setCancelled(true);
        item.remove();
        fountainItems.remove(item);

        int remaining = fountainItems.size();
        if (remaining < 0) remaining = 0;

        String pickupMsg = plugin.getConfig().getString("events.broadcast.money_pickup", "");
        if (pickupMsg != null && !pickupMsg.isEmpty()) {
            pickupMsg = pickupMsg.replace("%remaining%", String.valueOf(remaining));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', pickupMsg));
        }

        if (fountainItems.isEmpty() && currentEvent != null) {
            currentEvent.endEvent(false);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> false);
        event.getEntity().getWorld().getEntities().stream()
                .filter(e -> e instanceof Item && fountainItems.contains(e))
                .forEach(e -> e.teleport(e.getLocation()));
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().forEach(block -> {
            block.getWorld().getEntities().stream()
                    .filter(e -> e instanceof Item && fountainItems.contains(e))
                    .forEach(e -> e.teleport(e.getLocation()));
        });
    }

    @EventHandler
    public void onLightningStrike(LightningStrikeEvent event) {
        event.getWorld().getEntities().stream()
                .filter(e -> e instanceof Item && fountainItems.contains(e))
                .forEach(e -> e.teleport(e.getLocation()));
    }

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item && fountainItems.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
