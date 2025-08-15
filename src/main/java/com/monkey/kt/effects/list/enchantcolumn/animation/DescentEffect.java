package com.monkey.kt.effects.list.enchantcolumn.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.enchantcolumn.animation.util.ParticleUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

import java.util.*;

public class DescentEffect {

    private final KT plugin;
    private final World world;
    private final Location start, center;
    private final Player killer;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;

    public DescentEffect(KT plugin, World world, Location start, Location center, Player killer,
                         PotionEffectType effectType, int amplifier, int duration) {
        this.plugin = plugin;
        this.world = world;
        this.start = start;
        this.center = center;
        this.killer = killer;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public void start() {
        Vector direction = center.toVector().subtract(start.toVector()).normalize().multiply(0.7);
        List<Color> rainbow = Arrays.asList(Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.GREEN,
                Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.PURPLE);

        new BukkitRunnable() {
            int colorIndex = 0;
            Location current = start.clone();

            @Override
            public void run() {
                if (current.distanceSquared(center) <= 0.7) {
                    cancel();
                    world.playSound(current, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 0.7f);
                    new FinalEffect(plugin, world, current, killer, effectType, amplifier, duration).apply();
                    return;
                }

                Color color = rainbow.get(colorIndex++ % rainbow.size());
                current.add(direction);
                DustOptions dust = new DustOptions(color, 1.8f);
                world.spawnParticle(Particle.DUST, current, 3, 0, 0, 0, 0, dust);
                ParticleUtils.spawnTipCircle(world, current, 3.0, color);

                if (colorIndex % 7 == 0) {
                    world.playSound(current, Sound.ENTITY_BLAZE_HURT, 0.4f, 1.3f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
