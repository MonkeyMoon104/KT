package com.monkey.kt.effects.list.enchantcolumn.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.enchantcolumn.animation.util.ParticleArm;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import com.monkey.kt.utils.entity.EntityDataUtils;
import com.monkey.kt.utils.potion.PotionEffectUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import com.monkey.kt.utils.text.TextUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

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
        SchedulerWrapper.runTaskAtLocation(plugin, () -> {
            Collection<Entity> nearby = world.getNearbyEntities(center, radius, radius, radius);

            for (Entity e : nearby) {
                if (!(e instanceof Player player)) continue;

                if (effectType != null) {
                    player.addPotionEffect(new PotionEffect(effectType, duration * 20, amplifier - 1));
                    player.sendMessage(TextUtils.legacySection(
                                    plugin.getConfig().getString("messages.potion_set"))
                            .replace("%potion%", PotionEffectUtils.displayName(effectType))
                            .replace("%amplifier%", String.valueOf(amplifier)));
                } else {
                    player.sendMessage(TextUtils.legacySection(
                            plugin.getConfig().getString("messages.invalid_potion")));
                }
            }

            triggerExplosion();
        }, center, 0L);
    }

    private void triggerExplosion() {
        DamageConfig damageConfig = DamageUtils.getDamageConfig("enchantcolumn", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, center, damageConfig.getRadius(), damageConfig.getValue());
        }

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);

        int arms = 150;
        int effectDuration = 40;
        Random rand = new Random();

        List<ParticleArm> particleArms = new ArrayList<>();
        for (int i = 0; i < arms; i++) {
            Vector dir = new Vector((rand.nextDouble() - 0.5) * 6, (rand.nextDouble() - 0.2) * 5,
                    (rand.nextDouble() - 0.5) * 6).normalize().multiply(1.2);
            particleArms.add(new ParticleArm(center.clone(), dir));
        }

        SchedulerWrapper.runTaskAtLocation(plugin, () -> {
            try {
                for (Entity entity : world.getNearbyEntities(center, 6, 6, 6)) {
                    if (entity.equals(killer)) continue;
                    Vector knockback = entity.getLocation().toVector().subtract(center.toVector())
                            .normalize().multiply(2.5).setY(1.0);
                    entity.setVelocity(knockback);
                    EntityDataUtils.setBoolean(entity, plugin, "no_fall_damage", true);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Errore durante l'applicazione del knockback: " + e.getMessage());
            }
        }, center, 0L);

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.runTaskTimer(plugin, new Runnable() {
            int tick = 0;

            @Override
            public void run() {
                if (taskCompleted[0]) return;

                if (tick++ > effectDuration) {
                    taskCompleted[0] = true;
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                for (ParticleArm arm : particleArms) {
                    arm.location.add(arm.direction);
                    world.spawnParticle(Particle.ENCHANT, arm.location, 8, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }, 0L, 1L);
    }
}
