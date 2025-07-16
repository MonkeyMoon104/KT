package com.monkey.kt.effects.list.enchantcolumn.animation;

import com.monkey.kt.KT;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

import java.util.List;

public class MergeEffect {

    private final KT plugin;
    private final World world;
    private final Location center;
    private final List<List<Location>> columns;
    private final Player killer;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;

    public MergeEffect(KT plugin, World world, Location center, List<List<Location>> columns, Player killer,
                       PotionEffectType effectType, int amplifier, int duration) {
        this.plugin = plugin;
        this.world = world;
        this.center = center;
        this.columns = columns;
        this.killer = killer;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public void start() {
        Location mergePoint = center.clone().add(0, 15, 0);
        int mergeTicks = 20;

        Vector[] mergeVectors = new Vector[columns.size()];
        Location[] positions = new Location[columns.size()];

        for (int i = 0; i < columns.size(); i++) {
            positions[i] = columns.get(i).get(columns.get(i).size() - 1).clone();
            mergeVectors[i] = mergePoint.clone().subtract(positions[i]).toVector().multiply(1.0 / mergeTicks);
        }

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick > mergeTicks) {
                    cancel();
                    world.playSound(mergePoint, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
                    new DescentEffect(plugin, world, mergePoint, center, killer, effectType, amplifier, duration).start();
                    return;
                }

                DustOptions greenDust = new DustOptions(Color.GREEN, 1.5f);
                for (int i = 0; i < positions.length; i++) {
                    positions[i].add(mergeVectors[i]);
                    world.spawnParticle(Particle.REDSTONE, positions[i], 3, 0, 0, 0, 0, greenDust);
                }

                if (tick % 5 == 0) {
                    world.playSound(mergePoint, Sound.BLOCK_GLASS_BREAK, 0.6f, 1.2f);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
}
