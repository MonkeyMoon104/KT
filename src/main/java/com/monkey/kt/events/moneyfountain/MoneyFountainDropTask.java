package com.monkey.kt.events.moneyfountain;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class MoneyFountainDropTask extends BukkitRunnable {

    private final KT plugin;
    private final EconomyManager eco;
    private final World world;
    private final Location location;
    private final Material material;
    private final int dropAmount;
    private int count = 0;

    private final Random random = new Random();

    public MoneyFountainDropTask(KT plugin, EconomyManager eco, World world, Location location, Material material, int dropAmount) {
        this.plugin = plugin;
        this.eco = eco;
        this.world = world;
        this.location = location;
        this.material = material;
        this.dropAmount = dropAmount;
    }

    @Override
    public void run() {
        if (count >= dropAmount) {
            cancel();
            return;
        }

        ItemStack stack = new ItemStack(material, 1);
        stack = stack.asOne();
        stack.editMeta(meta -> {
            meta.setDisplayName("§r");
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "moneyfountain-uuid"),
                    PersistentDataType.STRING,
                    java.util.UUID.randomUUID().toString()
            );
        });

        Item item = world.dropItemNaturally(location, stack);

        item.setVelocity(item.getVelocity().setX(random.nextGaussian() * 0.3)
                .setY(0.5 + random.nextDouble() * 0.5)
                .setZ(random.nextGaussian() * 0.3));

        MoneyFountainListener.registerItem(item);

        if (material == Material.EMERALD) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!item.isValid() || item.isOnGround()) {
                        cancel();
                        return;
                    }

                    item.getWorld().spawnParticle(
                            Particle.HAPPY_VILLAGER,
                            item.getLocation().add(0, 0.1, 0),
                            2,
                            0.1, 0.1, 0.1,
                            0.02
                    );
                }
            }.runTaskTimer(plugin, 0L, 2L);
        }

        count++;
    }
}
