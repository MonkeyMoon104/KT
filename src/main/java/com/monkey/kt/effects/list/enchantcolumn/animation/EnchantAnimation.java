package com.monkey.kt.effects.list.enchantcolumn.animation;

import com.monkey.kt.KT;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

import java.util.*;

public class EnchantAnimation {

    private final KT plugin;
    private final World world;
    private final Location center;
    private final Player killer;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;

    public EnchantAnimation(KT plugin, World world, Location center, Player killer,
                            PotionEffectType effectType, int amplifier, int duration) {
        this.plugin = plugin;
        this.world = world;
        this.center = center.clone().add(0, 1.0, 0);
        this.killer = killer;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public void start() {
        world.playSound(center, Sound.ENTITY_PHANTOM_SWOOP, 1.0f, 1.4f);

        Vector[] directions = new Vector[]{
                new Vector(1, 0, 0),
                new Vector(-1, 0, 0),
                new Vector(0, 0, 1),
                new Vector(0, 0, -1),
                new Vector(1, 0, 1).normalize(),
                new Vector(-1, 0, -1).normalize()
        };

        int distance = 5, height = 10;
        List<List<Location>> columns = new ArrayList<>();

        for (Vector dir : directions) {
            List<Location> column = new ArrayList<>();
            Location base = center.clone().add(dir.clone().multiply(distance));
            for (int i = 0; i < height; i++) {
                column.add(base.clone().add(0, i, 0));
            }
            columns.add(column);
        }

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick > height) {
                    cancel();
                    world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                    new MergeEffect(plugin, world, center, columns, killer, effectType, amplifier, duration).start();
                    return;
                }

                DustOptions greenDust = new DustOptions(Color.GREEN, 1.5f);
                for (List<Location> column : columns) {
                    if (tick < column.size()) {
                        world.spawnParticle(Particle.REDSTONE, column.get(tick), 1, 0, 0, 0, 0, greenDust);
                    }
                }
                if (tick % 4 == 0) {
                    world.playSound(center, Sound.BLOCK_GRASS_STEP, 0.4f, 1.5f);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
}
