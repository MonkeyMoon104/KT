package com.monkey.kt.effects.custom;

import com.monkey.kt.KT;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomEffectConfig {

    private final YamlConfiguration config;
    private final String fileName;
    private final KT plugin;

    private String id;
    private String name;
    private List<String> description;
    private Material icon;
    private double price;
    private String permission;

    private boolean soundsEnabled;
    private List<SoundData> sounds;

    private boolean particlesEnabled;
    private List<ParticleData> particles;

    private boolean patternsEnabled;
    private List<PatternData> patterns;

    private boolean damageEnabled;
    private double damageValue;
    private double damageRadius;
    private int damageDelay;

    private boolean potionsEnabled;
    private List<PotionData> killerPotions;
    private List<PotionData> nearbyPotions;
    private double potionRadius;

    private boolean projectilesEnabled;
    private List<ProjectileData> projectiles;

    private boolean blocksEnabled;
    private boolean blocksTemporary;
    private int blocksRestoreDelay;
    private List<BlockData> blockPlacements;

    private boolean entitiesEnabled;
    private List<EntityData> entities;

    private boolean sequenceEnabled;
    private List<SequenceStep> sequenceSteps;

    private String killerMessage;
    private String victimMessage;
    private String broadcastMessage;
    private boolean actionBarEnabled;
    private String actionBarKiller;
    private String actionBarVictim;

    private int cooldown;
    private boolean weaponRequired;
    private List<Material> requiredWeapons;
    private String requiredKillType;

    public CustomEffectConfig(YamlConfiguration config, String fileName, KT plugin) {
        this.config = config;
        this.fileName = fileName;
        this.plugin = plugin;
        load();
    }

    private void load() {
        this.id = config.getString("effect.id", "unknown");
        this.name = config.getString("effect.name", "&7Unknown Effect");
        this.description = config.getStringList("effect.description");

        String iconStr = config.getString("effect.icon", "STONE");
        this.icon = Material.matchMaterial(iconStr);
        if (this.icon == null) this.icon = Material.STONE;

        this.price = config.getDouble("effect.price", 0);
        this.permission = config.getString("effect.permission", "");

        loadSounds();

        loadParticles();

        loadPatterns();

        loadDamage();

        loadPotions();

        loadProjectiles();

        loadBlocks();

        loadEntities();

        loadSequence();

        loadMessages();

        loadSettings();
    }

    private void loadSounds() {
        soundsEnabled = config.getBoolean("sounds.enabled", false);
        sounds = new ArrayList<>();

        if (soundsEnabled && config.contains("sounds.sounds")) {
            List<?> soundList = config.getList("sounds.sounds");
            if (soundList != null) {
                for (Object obj : soundList) {
                    if (obj instanceof ConfigurationSection) {
                        ConfigurationSection section = (ConfigurationSection) obj;
                        sounds.add(new SoundData(
                                section.getString("sound"),
                                section.getDouble("volume", 1.0),
                                section.getDouble("pitch", 1.0),
                                section.getInt("delay", 0)
                        ));
                    }
                }
            }
        }
    }

    private void loadParticles() {
        particlesEnabled = config.getBoolean("particles.enabled", false);
        particles = new ArrayList<>();

        if (particlesEnabled && config.contains("particles.effects")) {
            List<?> particleList = config.getList("particles.effects");
            if (particleList != null) {
                for (Object obj : particleList) {
                    if (obj instanceof ConfigurationSection) {
                        ConfigurationSection section = (ConfigurationSection) obj;

                        ConfigurationSection offset = section.getConfigurationSection("offset");
                        double offsetX = offset != null ? offset.getDouble("x", 0) : 0;
                        double offsetY = offset != null ? offset.getDouble("y", 0) : 0;
                        double offsetZ = offset != null ? offset.getDouble("z", 0) : 0;

                        ParticleData data = new ParticleData(
                                section.getString("type"),
                                section.getInt("count", 10),
                                offsetX, offsetY, offsetZ,
                                section.getDouble("speed", 0.01),
                                section.getInt("delay", 0),
                                section.getInt("duration", 20),
                                section.getInt("interval", 2)
                        );

                        if (section.contains("color")) {
                            ConfigurationSection color = section.getConfigurationSection("color");
                            if (color != null) {
                                data.setColor(
                                        color.getInt("red", 255),
                                        color.getInt("green", 255),
                                        color.getInt("blue", 255)
                                );
                                data.setSize(section.getDouble("size", 1.0));
                            }
                        }

                        particles.add(data);
                    }
                }
            }
        }
    }

    private void loadPatterns() {
        patternsEnabled = config.getBoolean("patterns.enabled", false);
        patterns = new ArrayList<>();

        if (patternsEnabled && config.contains("patterns.patterns")) {
            List<?> patternList = config.getList("patterns.patterns");
            if (patternList != null) {
                for (Object obj : patternList) {
                    if (obj instanceof ConfigurationSection) {
                        ConfigurationSection section = (ConfigurationSection) obj;
                        patterns.add(new PatternData(
                                section.getString("type"),
                                section.getString("particle"),
                                section.getDouble("radius", 3.0),
                                section.getInt("points", 30),
                                section.getInt("duration", 60),
                                section.getDouble("speed", 0.5),
                                section.getDouble("height", 0)
                        ));
                    }
                }
            }
        }
    }

    private void loadDamage() {
        damageEnabled = config.getBoolean("damage.enabled", false);
        damageValue = config.getDouble("damage.value", 5.0);
        damageRadius = config.getDouble("damage.radius", 5.0);
        damageDelay = config.getInt("damage.delay", 0);
    }

    private void loadPotions() {
        potionsEnabled = config.getBoolean("potions.enabled", false);
        killerPotions = new ArrayList<>();
        nearbyPotions = new ArrayList<>();

        if (potionsEnabled) {
            if (config.contains("potions.killer")) {
                List<?> killerList = config.getList("potions.killer");
                if (killerList != null) {
                    for (Object obj : killerList) {
                        if (obj instanceof ConfigurationSection) {
                            ConfigurationSection section = (ConfigurationSection) obj;
                            killerPotions.add(new PotionData(
                                    section.getString("type"),
                                    section.getInt("amplifier", 1),
                                    section.getInt("duration", 100)
                            ));
                        }
                    }
                }
            }

            if (config.contains("potions.nearby")) {
                ConfigurationSection nearby = config.getConfigurationSection("potions.nearby");
                if (nearby != null) {
                    potionRadius = nearby.getDouble("radius", 5.0);
                    List<?> nearbyList = nearby.getList("effects");
                    if (nearbyList != null) {
                        for (Object obj : nearbyList) {
                            if (obj instanceof ConfigurationSection) {
                                ConfigurationSection section = (ConfigurationSection) obj;
                                nearbyPotions.add(new PotionData(
                                        section.getString("type"),
                                        section.getInt("amplifier", 1),
                                        section.getInt("duration", 100)
                                ));
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadProjectiles() {
        projectilesEnabled = config.getBoolean("projectiles.enabled", false);
        projectiles = new ArrayList<>();

        if (projectilesEnabled && config.contains("projectiles.launch")) {
            List<?> projectileList = config.getList("projectiles.launch");
            if (projectileList != null) {
                for (Object obj : projectileList) {
                    if (obj instanceof ConfigurationSection) {
                        ConfigurationSection section = (ConfigurationSection) obj;
                        projectiles.add(new ProjectileData(
                                section.getString("type"),
                                section.getInt("count", 1),
                                section.getDouble("speed", 1.0),
                                section.getInt("spread", 45),
                                section.getBoolean("gravity", true),
                                section.getDouble("damage", 0.0),
                                section.getBoolean("remove_on_hit", true),
                                section.getString("trail_particle", ""),
                                section.getInt("delay", 0)
                        ));
                    }
                }
            }
        }
    }

    private void loadBlocks() {
        blocksEnabled = config.getBoolean("blocks.enabled", false);
        blocksTemporary = config.getBoolean("blocks.temporary", true);
        blocksRestoreDelay = config.getInt("blocks.restore_delay", 100);
        blockPlacements = new ArrayList<>();

        if (blocksEnabled && config.contains("blocks.place")) {
            List<?> blockList = config.getList("blocks.place");
            if (blockList != null) {
                for (Object obj : blockList) {
                    if (obj instanceof ConfigurationSection) {
                        ConfigurationSection section = (ConfigurationSection) obj;
                        blockPlacements.add(new BlockData(
                                section.getString("material"),
                                section.getString("pattern", "SINGLE"),
                                section.getDouble("radius", 0),
                                section.getInt("offset.x", 0),
                                section.getInt("offset.y", 0),
                                section.getInt("offset.z", 0)
                        ));
                    }
                }
            }
        }
    }

    private void loadEntities() {
        entitiesEnabled = config.getBoolean("entities.enabled", false);
        entities = new ArrayList<>();

        if (entitiesEnabled && config.contains("entities.spawn")) {
            List<?> entityList = config.getList("entities.spawn");
            if (entityList != null) {
                for (Object obj : entityList) {
                    if (obj instanceof ConfigurationSection) {
                        ConfigurationSection section = (ConfigurationSection) obj;
                        entities.add(new EntityData(
                                section.getString("type"),
                                section.getBoolean("effect_only", true),
                                section.getInt("delay", 0),
                                section.getInt("duration", 100),
                                section.getString("custom_name", ""),
                                section.getBoolean("visible", true),
                                section.getBoolean("gravity", true),
                                section.getBoolean("invulnerable", true)
                        ));
                    }
                }
            }
        }
    }

    private void loadSequence() {
        sequenceEnabled = config.getBoolean("sequence.enabled", false);
        sequenceSteps = new ArrayList<>();

        if (sequenceEnabled && config.contains("sequence.steps")) {
            List<?> stepList = config.getList("sequence.steps");

            if (stepList != null) {
                for (Object obj : stepList) {
                    ConfigurationSection section = null;

                    if (obj instanceof ConfigurationSection) {
                        section = (ConfigurationSection) obj;
                    } else if (obj instanceof Map) {
                        Map<?, ?> stepMap = (Map<?, ?>) obj;
                        org.bukkit.configuration.file.YamlConfiguration tempConfig =
                                new org.bukkit.configuration.file.YamlConfiguration();

                        for (Map.Entry<?, ?> entry : stepMap.entrySet()) {
                            tempConfig.set(entry.getKey().toString(), entry.getValue());
                        }
                        section = tempConfig;
                    }

                    if (section != null) {
                        int tick = section.getInt("tick", 0);
                        List<?> actions = section.getList("actions");

                        sequenceSteps.add(new SequenceStep(tick, actions));
                    }
                }
            }
        }
    }

    private void loadMessages() {
        killerMessage = config.getString("messages.killer", "");
        victimMessage = config.getString("messages.victim", "");
        broadcastMessage = config.getString("messages.broadcast", "");

        actionBarEnabled = config.getBoolean("messages.action_bar.enabled", false);
        actionBarKiller = config.getString("messages.action_bar.killer", "");
        actionBarVictim = config.getString("messages.action_bar.victim", "");
    }

    private void loadSettings() {
        cooldown = config.getInt("cooldown", 0);

        weaponRequired = config.getBoolean("requirements.weapon.enabled", false);
        requiredWeapons = new ArrayList<>();
        if (weaponRequired) {
            List<String> materials = config.getStringList("requirements.weapon.materials");
            for (String mat : materials) {
                Material material = Material.matchMaterial(mat);
                if (material != null) {
                    requiredWeapons.add(material);
                }
            }
        }

        requiredKillType = config.getString("requirements.kill_type.type", "ANY");
    }

    public boolean isValid() {
        return id != null && !id.isEmpty() && icon != null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getDescription() { return description; }
    public Material getIcon() { return icon; }
    public double getPrice() { return price; }
    public String getPermission() { return permission; }

    public boolean isSoundsEnabled() { return soundsEnabled; }
    public List<SoundData> getSounds() { return sounds; }

    public boolean isParticlesEnabled() { return particlesEnabled; }
    public List<ParticleData> getParticles() { return particles; }

    public boolean isPatternsEnabled() { return patternsEnabled; }
    public List<PatternData> getPatterns() { return patterns; }

    public boolean isDamageEnabled() { return damageEnabled; }
    public double getDamageValue() { return damageValue; }
    public double getDamageRadius() { return damageRadius; }
    public int getDamageDelay() { return damageDelay; }

    public boolean isPotionsEnabled() { return potionsEnabled; }
    public List<PotionData> getKillerPotions() { return killerPotions; }
    public List<PotionData> getNearbyPotions() { return nearbyPotions; }
    public double getPotionRadius() { return potionRadius; }

    public boolean isProjectilesEnabled() { return projectilesEnabled; }
    public List<ProjectileData> getProjectiles() { return projectiles; }

    public boolean isBlocksEnabled() { return blocksEnabled; }
    public boolean isBlocksTemporary() { return blocksTemporary; }
    public int getBlocksRestoreDelay() { return blocksRestoreDelay; }
    public List<BlockData> getBlockPlacements() { return blockPlacements; }

    public boolean isEntitiesEnabled() { return entitiesEnabled; }
    public List<EntityData> getEntities() { return entities; }

    public boolean isSequenceEnabled() { return sequenceEnabled; }
    public List<SequenceStep> getSequenceSteps() { return sequenceSteps; }

    public String getKillerMessage() { return killerMessage; }
    public String getVictimMessage() { return victimMessage; }
    public String getBroadcastMessage() { return broadcastMessage; }
    public boolean isActionBarEnabled() { return actionBarEnabled; }
    public String getActionBarKiller() { return actionBarKiller; }
    public String getActionBarVictim() { return actionBarVictim; }

    public int getCooldown() { return cooldown; }
    public boolean isWeaponRequired() { return weaponRequired; }
    public List<Material> getRequiredWeapons() { return requiredWeapons; }
    public String getRequiredKillType() { return requiredKillType; }

    public static class SoundData {
        private final String sound;
        private final double volume;
        private final double pitch;
        private final int delay;

        public SoundData(String sound, double volume, double pitch, int delay) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.delay = delay;
        }

        public Sound getSound() {
            try {
                return Sound.valueOf(sound.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public double getVolume() { return volume; }
        public double getPitch() { return pitch; }
        public int getDelay() { return delay; }
    }

    public static class ParticleData {
        private final String type;
        private final int count;
        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;
        private final double speed;
        private final int delay;
        private final int duration;
        private final int interval;

        private int red = 255;
        private int green = 255;
        private int blue = 255;
        private double size = 1.0;

        public ParticleData(String type, int count, double offsetX, double offsetY, double offsetZ,
                            double speed, int delay, int duration, int interval) {
            this.type = type;
            this.count = count;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.speed = speed;
            this.delay = delay;
            this.duration = duration;
            this.interval = interval;
        }

        public void setColor(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public void setSize(double size) {
            this.size = size;
        }

        public Particle getParticle() {
            try {
                return Particle.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public int getCount() { return count; }
        public double getOffsetX() { return offsetX; }
        public double getOffsetY() { return offsetY; }
        public double getOffsetZ() { return offsetZ; }
        public double getSpeed() { return speed; }
        public int getDelay() { return delay; }
        public int getDuration() { return duration; }
        public int getInterval() { return interval; }
        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
        public double getSize() { return size; }
    }

    public static class PatternData {
        private final String type;
        private final String particle;
        private final double radius;
        private final int points;
        private final int duration;
        private final double speed;
        private final double height;

        public PatternData(String type, String particle, double radius, int points,
                           int duration, double speed, double height) {
            this.type = type;
            this.particle = particle;
            this.radius = radius;
            this.points = points;
            this.duration = duration;
            this.speed = speed;
            this.height = height;
        }

        public String getType() { return type; }
        public Particle getParticle() {
            try {
                return Particle.valueOf(particle.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Particle.FLAME;
            }
        }
        public double getRadius() { return radius; }
        public int getPoints() { return points; }
        public int getDuration() { return duration; }
        public double getSpeed() { return speed; }
        public double getHeight() { return height; }
    }

    public static class PotionData {
        private final String type;
        private final int amplifier;
        private final int duration;

        public PotionData(String type, int amplifier, int duration) {
            this.type = type;
            this.amplifier = amplifier;
            this.duration = duration;
        }

        public PotionEffectType getType() {
            return PotionEffectType.getByName(type.toUpperCase());
        }
        public int getAmplifier() { return amplifier; }
        public int getDuration() { return duration; }
    }

    public static class ProjectileData {
        private final String type;
        private final int count;
        private final double speed;
        private final int spread;
        private final boolean gravity;
        private final double damage;
        private final boolean removeOnHit;
        private final String trailParticle;
        private final int delay;

        public ProjectileData(String type, int count, double speed, int spread, boolean gravity,
                              double damage, boolean removeOnHit, String trailParticle, int delay) {
            this.type = type;
            this.count = count;
            this.speed = speed;
            this.spread = spread;
            this.gravity = gravity;
            this.damage = damage;
            this.removeOnHit = removeOnHit;
            this.trailParticle = trailParticle;
            this.delay = delay;
        }

        public String getType() { return type; }
        public int getCount() { return count; }
        public double getSpeed() { return speed; }
        public int getSpread() { return spread; }
        public boolean hasGravity() { return gravity; }
        public double getDamage() { return damage; }
        public boolean shouldRemoveOnHit() { return removeOnHit; }
        public String getTrailParticle() { return trailParticle; }
        public int getDelay() { return delay; }
    }

    public static class BlockData {
        private final String material;
        private final String pattern;
        private final double radius;
        private final int offsetX;
        private final int offsetY;
        private final int offsetZ;

        public BlockData(String material, String pattern, double radius,
                         int offsetX, int offsetY, int offsetZ) {
            this.material = material;
            this.pattern = pattern;
            this.radius = radius;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        public Material getMaterial() {
            return Material.matchMaterial(material.toUpperCase());
        }
        public String getPattern() { return pattern; }
        public double getRadius() { return radius; }
        public int getOffsetX() { return offsetX; }
        public int getOffsetY() { return offsetY; }
        public int getOffsetZ() { return offsetZ; }
    }

    public static class EntityData {
        private final String type;
        private final boolean effectOnly;
        private final int delay;
        private final int duration;
        private final String customName;
        private final boolean visible;
        private final boolean gravity;
        private final boolean invulnerable;

        public EntityData(String type, boolean effectOnly, int delay, int duration,
                          String customName, boolean visible, boolean gravity, boolean invulnerable) {
            this.type = type;
            this.effectOnly = effectOnly;
            this.delay = delay;
            this.duration = duration;
            this.customName = customName;
            this.visible = visible;
            this.gravity = gravity;
            this.invulnerable = invulnerable;
        }

        public String getType() { return type; }
        public boolean isEffectOnly() { return effectOnly; }
        public int getDelay() { return delay; }
        public int getDuration() { return duration; }
        public String getCustomName() { return customName; }
        public boolean isVisible() { return visible; }
        public boolean hasGravity() { return gravity; }
        public boolean isInvulnerable() { return invulnerable; }
    }

    public static class SequenceStep {
        private final int tick;
        private final List<?> actions;

        public SequenceStep(int tick, List<?> actions) {
            this.tick = tick;
            this.actions = actions != null ? actions : new ArrayList<>();
        }

        public int getTick() { return tick; }
        public List<?> getActions() { return actions; }
    }
}