package com.monkey.kt.effects.list.fireworks;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.utils.SensitiveBlockUtils;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class FireworksEffect implements KillEffect {

    private final KT plugin;

    public FireworksEffect(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(Player killer, Location target) {
        World world = target.getWorld();
        if (world == null) return;

        boolean allowStructure = plugin.getConfig().getBoolean("effects.fireworks.structure", true);

        Location center = target.clone().add(0, 0.5, 0);
        List<Location> launchPoints = generateLaunchPoints(center, 16);
        Random random = new Random();
        Map<Location, Material> originalBlocks = new HashMap<>();

        BukkitRunnable rotatingCircles = new BukkitRunnable() {
            double angle = 0;
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 80) {
                    cancel();
                    return;
                }

                for (double a = 0; a < 2 * Math.PI; a += Math.PI / 32) {
                    for (double radius : new double[]{3.5, 5.0}) {
                        double x = Math.cos(a + angle) * radius;
                        double z = Math.sin(a + angle) * radius;
                        Location loc = center.clone().add(x, 0.1, z);
                        world.spawnParticle(Particle.REDSTONE, loc, 3,
                                new Particle.DustOptions(randomColor(), 1.4F));
                    }
                }
                angle += Math.PI / 48;
                ticks++;
            }
        };
        rotatingCircles.runTaskTimer(plugin, 0L, 2L);

        final int totalPlatforms = launchPoints.size();
        final int[] finishedPlatforms = {0};

        for (int i = 0; i < launchPoints.size(); i++) {
            Location launchLoc = launchPoints.get(i);
            Vector direction = randomObliqueVector();

            int delay = 20 + (i * 8);

            Block platform = launchLoc.clone().subtract(0, 1, 0).getBlock();
            Location platformLoc = platform.getLocation();
            Material original = platform.getType();
            originalBlocks.put(platformLoc, original);

            Block blockAbove = platform.getRelative(BlockFace.UP);

            if (allowStructure
                    && !SensitiveBlockUtils.isSensitive(platform)
                    && !SensitiveBlockUtils.isSensitive(blockAbove)) {

                WorldGuardUtils.runWithWorldGuardBypass(platformLoc, () -> platform.setType(Material.GLOWSTONE));
            }


            BukkitRunnable whiteParticlesTask = new BukkitRunnable() {
                @Override
                public void run() {
                    Block currentBlock = platformLoc.getBlock();
                    if (currentBlock.getType() != Material.GLOWSTONE) {
                        cancel();
                        return;
                    }
                    world.spawnParticle(Particle.FIREWORKS_SPARK, launchLoc.clone().add(0, 0.2, 0),
                            10, 0.2, 0.02, 0.2, 0.001);
                }
            };
            whiteParticlesTask.runTaskTimer(plugin, 0L, 2L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Firework firework = world.spawn(launchLoc, Firework.class);
                    FireworkMeta meta = firework.getFireworkMeta();

                    FireworkEffect effect = FireworkEffect.builder()
                            .flicker(true)
                            .trail(true)
                            .withColor(randomColor(), randomColor(), randomColor())
                            .withFade(randomColor())
                            .with(FireworkEffect.Type.BURST)
                            .build();

                    meta.addEffect(effect);
                    meta.setPower(0);
                    firework.setFireworkMeta(meta);
                    firework.setSilent(true);
                    firework.setVelocity(direction.multiply(0.6));

                    world.playSound(launchLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.5f, 1.5f);
                    world.playSound(launchLoc, Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Material oldType = originalBlocks.getOrDefault(platformLoc, Material.AIR);
                            WorldGuardUtils.runWithWorldGuardBypass(platformLoc, () -> platform.setType(oldType));
                            world.playSound(launchLoc, Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);

                        }
                    }.runTaskLater(plugin, 5L);

                    finishedPlatforms[0]++;
                    if (finishedPlatforms[0] >= totalPlatforms) {
                        rotatingCircles.cancel();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                int finalFireworksCount = 10;
                                double radius = 4.5;

                                for (int i = 0; i < finalFireworksCount; i++) {
                                    double angle = 2 * Math.PI * i / finalFireworksCount;
                                    Location loc = center.clone().add(Math.cos(angle) * radius, -0.3, Math.sin(angle) * radius);

                                    Firework fw = world.spawn(loc, Firework.class);
                                    FireworkMeta meta = fw.getFireworkMeta();

                                    FireworkEffect effect = FireworkEffect.builder()
                                            .flicker(true)
                                            .trail(true)
                                            .withColor(randomColor(), randomColor(), Color.WHITE)
                                            .withFade(Color.FUCHSIA, Color.AQUA)
                                            .with(FireworkEffect.Type.BALL_LARGE)
                                            .build();

                                    meta.addEffect(effect);
                                    meta.setPower(0);
                                    fw.setFireworkMeta(meta);
                                    fw.setSilent(false);
                                    fw.setVelocity(new Vector(0, 0.1, 0));

                                    world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 5.0f, 0.8f);
                                }

                                Firework special = world.spawn(center.clone().add(0, 0.5, 0), Firework.class);
                                FireworkMeta specialMeta = special.getFireworkMeta();

                                for (int i = 0; i < 7; i++) {
                                    specialMeta.addEffect(FireworkEffect.builder()
                                            .flicker(true)
                                            .trail(true)
                                            .withColor(Color.RED, Color.ORANGE, Color.YELLOW)
                                            .withFade(Color.FUCHSIA, Color.AQUA)
                                            .with(FireworkEffect.Type.STAR)
                                            .build());

                                    specialMeta.addEffect(FireworkEffect.builder()
                                            .flicker(true)
                                            .trail(true)
                                            .withColor(Color.AQUA, Color.PURPLE, Color.LIME)
                                            .withFade(Color.WHITE)
                                            .with(FireworkEffect.Type.BALL_LARGE)
                                            .build());

                                    specialMeta.addEffect(FireworkEffect.builder()
                                            .flicker(true)
                                            .trail(true)
                                            .withColor(Color.GREEN, Color.BLUE, Color.YELLOW)
                                            .withFade(Color.TEAL, Color.FUCHSIA)
                                            .with(FireworkEffect.Type.BALL)
                                            .build());
                                }

                                specialMeta.setPower(0);
                                special.setFireworkMeta(specialMeta);
                                special.setSilent(false);
                                special.setVelocity(new Vector(0, 1.2, 0));

                                world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 8.0f, 0.6f);
                                world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 6.0f, 0.5f);
                                world.playSound(center, Sound.ITEM_TOTEM_USE, 10.0f, 0.7f);

                            }
                        }.runTaskLater(plugin, 10L);
                    }
                }
            }.runTaskLater(plugin, delay);
        }
    }

    private List<Location> generateLaunchPoints(Location center, int count) {
        List<Location> locations = new ArrayList<>();
        double radius = 4.0;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            locations.add(center.clone().add(x, 0, z));
        }
        return locations;
    }

    private Vector randomObliqueVector() {
        Random rand = new Random();
        double x = (rand.nextDouble() - 0.5) * 1.2;
        double y = 0.3 + rand.nextDouble() * 0.3;
        double z = (rand.nextDouble() - 0.5) * 1.2;
        return new Vector(x, y, z).normalize();
    }

    private Color randomColor() {
        Color[] colors = {
                Color.RED, Color.ORANGE, Color.YELLOW,
                Color.GREEN, Color.BLUE, Color.AQUA,
                Color.FUCHSIA, Color.WHITE, Color.PURPLE
        };
        return colors[new Random().nextInt(colors.length)];
    }
}