package com.monkey.kt.events.meteorshower;

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

public class MeteorShowerListener implements Listener {

    private static final Set<Item> meteorItems = new HashSet<>();
    private static MeteorShowerEvent currentEvent;
    private final KT plugin;

    public MeteorShowerListener(KT plugin) {
        this.plugin = plugin;
    }

    public static void registerItem(Item item) {
        meteorItems.add(item);
    }

    public static void removeAllItems() {
        for (Item item : meteorItems) {
            if (item != null && item.isValid()) item.remove();
        }
        meteorItems.clear();
    }

    public static void setCurrentEvent(MeteorShowerEvent event) {
        currentEvent = event;
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        EconomyManager eco = plugin.getEconomyManager();
        Item item = event.getItem();
        if (!meteorItems.contains(item)) return;

        Player player = event.getPlayer();
        double moneyValue = plugin.getConfig().getDouble("events.list.meteor_shower.settings.money_value", 2000);

        eco.add(player, moneyValue);

        event.setCancelled(true);
        item.remove();
        meteorItems.remove(item);

        int remaining = meteorItems.size();
        if (remaining < 0) remaining = 0;

        String pickupMsg = plugin.getConfig().getString("events.broadcast.money_pickup", "");
        if (pickupMsg != null && !pickupMsg.isEmpty()) {
            pickupMsg = pickupMsg.replace("%remaining%", String.valueOf(remaining));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', pickupMsg));
        }

        if (meteorItems.isEmpty() && currentEvent != null) {
            currentEvent.endEvent(false);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> false);
        event.getEntity().getWorld().getEntities().stream()
                .filter(e -> e instanceof Item && meteorItems.contains(e))
                .forEach(e -> e.teleport(e.getLocation()));
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().forEach(block -> {
            block.getWorld().getEntities().stream()
                    .filter(e -> e instanceof Item && meteorItems.contains(e))
                    .forEach(e -> e.teleport(e.getLocation()));
        });
    }

    @EventHandler
    public void onLightningStrike(LightningStrikeEvent event) {
        event.getWorld().getEntities().stream()
                .filter(e -> e instanceof Item && meteorItems.contains(e))
                .forEach(e -> e.teleport(e.getLocation()));
    }

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item && meteorItems.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
