package com.monkey.kt.effects.list.enchantcolumn.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.enchantcolumn.animation.util.ParticleArm;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class FinalEffect {

    private final KT plugin;
    private final World world;
    private final Location center;
    private final Player killer;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;

    public FinalEffect(KT plugin, World world, Location center, Player killer,
                       PotionEffectType effectType, int amplifier, int duration) {
        this.plugin = plugin;
        this.world = world;
        this.center = center;
        this.killer = killer;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public void apply() {
        double radius = 3.0;
        Collection<Entity> nearby = world.getNearbyEntities(center, radius, radius, radius);

        for (Entity e : nearby) {
            if (!(e instanceof Player)) continue;
            Player player = (Player) e;
            if (effectType != null) {
                player.addPotionEffect(new PotionEffect(effectType, duration * 20, amplifier - 1));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("messages.potion_set"))
                        .replace("%potion%", effectType.getName())
                        .replace("%amplifier%", String.valueOf(amplifier)));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.invalid_potion")));
            }
        }

        triggerExplosion();
    }

    private void triggerExplosion() {
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);

        int arms = 150, duration = 40;
        Random rand = new Random();

        List<ParticleArm> particleArms = new ArrayList<>();
        for (int i = 0; i < arms; i++) {
            Vector dir = new Vector((rand.nextDouble() - 0.5) * 6, (rand.nextDouble() - 0.2) * 5,
                    (rand.nextDouble() - 0.5) * 6).normalize().multiply(1.2);
            particleArms.add(new ParticleArm(center.clone(), dir));
        }

        for (Entity entity : world.getNearbyEntities(center, 6, 6, 6)) {
            if (entity.equals(killer)) continue;
            Vector knockback = entity.getLocation().toVector().subtract(center.toVector())
                    .normalize().multiply(2.5).setY(1.0);
            entity.setVelocity(knockback);
        }

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick++ > duration) {
                    cancel();
                    return;
                }

                for (ParticleArm arm : particleArms) {
                    arm.location.add(arm.direction);
                    world.spawnParticle(Particle.ENCHANTMENT_TABLE, arm.location, 8, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
