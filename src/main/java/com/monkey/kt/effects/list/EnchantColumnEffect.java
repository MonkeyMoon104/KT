package com.monkey.kt.effects.list;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

import java.util.*;

public class EnchantColumnEffect implements KillEffect {

    private final KT plugin;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;

    public EnchantColumnEffect(KT plugin) {
        this.plugin = plugin;

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("effects.enchantcolumn.effectexplosion");
        if (section != null) {
            String typeName = section.getString("type", "REGENERATION");
            this.effectType = PotionEffectType.getByName(typeName.toUpperCase());
            this.amplifier = section.getInt("amplifier", 1);
            this.duration = section.getInt("duration", 10);
        } else {
            this.effectType = PotionEffectType.REGENERATION;
            this.amplifier = 1;
            this.duration = 10;
        }
    }

    @Override
    public void play(Player killer, Location target) {
        World world = target.getWorld();
        if (world == null) return;

        Location center = target.clone().add(0, 1.0, 0);
        world.playSound(center, Sound.ENTITY_PHANTOM_SWOOP, 1.0f, 1.4f);

        Vector[] directions = new Vector[] {
                new Vector(1, 0, 0),
                new Vector(-1, 0, 0),
                new Vector(0, 0, 1),
                new Vector(0, 0, -1),
                new Vector(1, 0, 1).normalize(),
                new Vector(-1, 0, -1).normalize()
        };

        int distance = 5;
        int height = 10;
        int columnLength = height;

        List<List<Location>> columns = new ArrayList<>();

        for (Vector dir : directions) {
            List<Location> column = new ArrayList<>();
            Location base = center.clone().add(dir.clone().multiply(distance));
            for (int i = 0; i < columnLength; i++) {
                Location loc = base.clone().add(0, i, 0);
                column.add(loc);
            }
            columns.add(column);
        }

        int ascentTicks = height;

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick > ascentTicks) {
                    cancel();
                    world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                    startMergeAndDescent(world, center, columns, killer);
                    return;
                }

                DustOptions greenDust = new DustOptions(Color.GREEN, 1.5f);
                for (List<Location> column : columns) {
                    if (tick < column.size()) {
                        Location loc = column.get(tick);
                        spawnBigDot(world, loc, greenDust);
                    }
                }
                if (tick % 4 == 0) {
                    world.playSound(center, Sound.BLOCK_GRASS_STEP, 0.4f, 1.5f);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    private void spawnBigDot(World world, Location center, DustOptions dust) {
        int points = 12;
        double radius = 0.5;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = center.clone().add(x, 0, z);
            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dust);
        }
    }

    private void startMergeAndDescent(World world, Location center, List<List<Location>> columns, Player killer) {
        Location mergePoint = center.clone().add(0, 15, 0);
        int mergeTicks = 20;

        Vector[] mergeVectors = new Vector[columns.size()];
        Location[] currentPositions = new Location[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            currentPositions[i] = columns.get(i).get(columns.get(i).size() - 1).clone();
            Vector dir = mergePoint.toVector().subtract(currentPositions[i].toVector()).multiply(1.0 / mergeTicks);
            mergeVectors[i] = dir;
        }

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick > mergeTicks) {
                    cancel();
                    world.playSound(mergePoint, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
                    startDescent(world, mergePoint, center, killer);
                    return;
                }

                DustOptions greenDust = new DustOptions(Color.GREEN, 1.5f);
                for (int i = 0; i < currentPositions.length; i++) {
                    currentPositions[i].add(mergeVectors[i]);
                    world.spawnParticle(Particle.REDSTONE, currentPositions[i], 3, 0, 0, 0, 0, greenDust);
                }
                if (tick % 5 == 0) {
                    world.playSound(mergePoint, Sound.BLOCK_GLASS_BREAK, 0.6f, 1.2f);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    private void startDescent(World world, Location start, Location center, Player killer) {
        Vector direction = center.toVector().subtract(start.toVector()).normalize().multiply(0.7);

        List<Color> rainbow = Arrays.asList(
                Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.GREEN,
                Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.PURPLE
        );

        new BukkitRunnable() {
            int colorIndex = 0;
            Location current = start.clone();

            @Override
            public void run() {
                if (current.distanceSquared(center) <= 0.7) {
                    cancel();
                    world.playSound(current, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 0.7f);
                    giveRegenAndExplode(world, current, killer);
                    return;
                }

                Color color = rainbow.get(colorIndex++ % rainbow.size());
                current.add(direction);
                DustOptions dust = new DustOptions(color, 1.8f);
                world.spawnParticle(Particle.REDSTONE, current, 3, 0, 0, 0, 0, dust);
                spawnTipCircle(world, current, 3.0, color);
                if (colorIndex % 7 == 0) {
                    world.playSound(current, Sound.ENTITY_BLAZE_HURT, 0.4f, 1.3f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnTipCircle(World world, Location tip, double radius, Color color) {
        int points = 30;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = tip.clone().add(x, 0, z);
            world.spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0, 0, new DustOptions(color, 1.5f));
        }
    }

    private void giveRegenAndExplode(World world, Location center, Player killer) {
        double radius = 3.0;
        Collection<Entity> nearby = world.getNearbyEntities(center, radius, radius, radius);

        if (effectType != null) {
            for (Entity e : nearby) {
                if (e instanceof Player) {
                    ((Player) e).addPotionEffect(new PotionEffect(effectType, duration * 20, amplifier - 1));
                    ((Player) e).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.potion_set"))
                            .replace("%potion%", effectType.getName())
                            .replace("%amplifier%", String.valueOf(amplifier)));
                }
            }
        } else {
            for (Entity e : nearby) {
                if (e instanceof Player) {
                    ((Player) e).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid_potion")));
                }
            }
        }

        triggerExplosionWithKnockback(world, center, killer);
    }

    private void triggerExplosionWithKnockback(World world, Location center, Player killer) {
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);

        int numberOfArms = 150;
        int ticksDuration = 40;
        Random random = new Random();

        List<ParticleArm> arms = new ArrayList<>();
        for (int i = 0; i < numberOfArms; i++) {
            Vector direction = new Vector(
                    (random.nextDouble() - 0.5) * 6,
                    (random.nextDouble() - 0.2) * 5,
                    (random.nextDouble() - 0.5) * 6
            ).normalize().multiply(1.2);

            arms.add(new ParticleArm(center.clone(), direction));
        }

        double knockbackStrength = 2.5;
        for (Entity entity : world.getNearbyEntities(center, 6, 6, 6)) {
            if (entity.equals(killer)) continue;
            Vector knockback = entity.getLocation().toVector().subtract(center.toVector()).normalize().multiply(knockbackStrength).setY(1.0);
            entity.setVelocity(knockback);
        }

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick++ > ticksDuration) {
                    cancel();
                    return;
                }

                for (ParticleArm arm : arms) {
                    arm.location.add(arm.direction);
                    world.spawnParticle(Particle.ENCHANTMENT_TABLE, arm.location, 8, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static class ParticleArm {
        Location location;
        Vector direction;

        ParticleArm(Location location, Vector direction) {
            this.location = location;
            this.direction = direction;
        }
    }
}
