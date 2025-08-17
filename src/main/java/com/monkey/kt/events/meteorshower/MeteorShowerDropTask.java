package com.monkey.kt.events.meteorshower;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class MeteorShowerDropTask extends BukkitRunnable {

    private final KT plugin;
    private final EconomyManager eco;
    private final World world;
    private final Player killer;
    private final Material material;
    private final int meteorCount;
    private int count = 0;
    private final Random random = new Random();

    public MeteorShowerDropTask(KT plugin, EconomyManager eco, World world, Player killer, Material material, int meteorCount) {
        this.plugin = plugin;
        this.eco = eco;
        this.world = world;
        this.killer = killer;
        this.material = material;
        this.meteorCount = meteorCount;
    }

    @Override
    public void run() {
        if (count >= meteorCount) {
            cancel();
            return;
        }

        ItemStack stack = new ItemStack(material, 1);
        stack.editMeta(meta -> {
            meta.setDisplayName("§r");
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "meteorshower-uuid"),
                    PersistentDataType.STRING,
                    java.util.UUID.randomUUID().toString()
            );
        });

        double radius = 5 + random.nextDouble() * 5;
        double angle = random.nextDouble() * 2 * Math.PI;
        double spawnX = killer.getLocation().getX() + Math.cos(angle) * radius;
        double spawnZ = killer.getLocation().getZ() + Math.sin(angle) * radius;
        double spawnY = killer.getLocation().getY() + 15 + random.nextDouble() * 5;

        Location spawnLoc = new Location(world, spawnX, spawnY, spawnZ);
        Item item = world.dropItem(spawnLoc, stack);

        MeteorShowerListener.registerItem(item);
        item.setPickupDelay(Integer.MAX_VALUE);

        Vector direction = killer.getLocation().clone().add(0, 0.5, 0).toVector().subtract(spawnLoc.toVector());
        direction.normalize();
        double speed = 0.8 + random.nextDouble() * 0.2;
        direction.multiply(speed);
        direction.setY(direction.getY() - 0.5);

        item.setVelocity(direction);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (item.isDead() || !item.isValid()) {
                    cancel();
                    return;
                }

                Location loc = item.getLocation();

                world.spawnParticle(Particle.END_ROD, loc, 3, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.FLAME, loc, 2, 0.1, 0.1, 0.1, 0.05);
                world.spawnParticle(Particle.SMOKE, loc, 1, 0.1, 0.1, 0.1, 0.01);
                world.spawnParticle(Particle.SPIT, loc, 1, 0.1, 0.1, 0.1, 0.01);

                world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.2f, 1.2f);
                item.setRotation(loc.getYaw() + 10, loc.getPitch() + 5);

                if (item.isOnGround()) {
                    world.spawnParticle(Particle.EXPLOSION, loc, 10, 0.5, 0.5, 0.5, 0.1);
                    world.spawnParticle(Particle.FIREWORK, loc, 20, 0.5, 0.5, 0.5, 0.05);
                    world.spawnParticle(Particle.FLASH, loc, 15, 0.3, 0.3, 0.3, 0.05);

                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                    world.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.2f);
                    world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.3f, 1.0f);

                    item.setPickupDelay(0);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        count++;
    }
}
