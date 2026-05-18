package com.monkey.kt.effects.custom;

import com.monkey.kt.KT;
import com.monkey.kt.utils.potion.PotionEffectUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class CustomEffectConfig {

    private final YamlConfiguration config;
    private final String fileName;
    private final KT plugin;

    private String id;
    private List<String> aliases;
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
        this.id = normalizeEffectId(config.getString("effect.id", "unknown"));
        this.aliases = loadAliases();
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
            for (ConfigurationSection section : getSectionList("sounds.sounds")) {
                sounds.add(new SoundData(
                        section.getString("sound"),
                        section.getDouble("volume", 1.0),
                        section.getDouble("pitch", 1.0),
                        section.getInt("delay", 0)
                ));
            }
        }
    }

    private void loadParticles() {
        particlesEnabled = config.getBoolean("particles.enabled", false);
        particles = new ArrayList<>();

        if (particlesEnabled && config.contains("particles.effects")) {
            for (ConfigurationSection section : getSectionList("particles.effects")) {
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

                applyColorSettings(section, data);
                particles.add(data);
            }
        }
    }

    private void loadPatterns() {
        patternsEnabled = config.getBoolean("patterns.enabled", false);
        patterns = new ArrayList<>();

        if (patternsEnabled && config.contains("patterns.patterns")) {
            for (ConfigurationSection section : getSectionList("patterns.patterns")) {
                PatternData patternData = new PatternData(
                        section.getString("type"),
                        section.getString("particle"),
                        section.getDouble("radius", 3.0),
                        section.getInt("points", 30),
                        section.getInt("duration", 60),
                        section.getDouble("speed", 0.5),
                        section.getDouble("height", 0)
                );

                applyColorSettings(section, patternData);
                patterns.add(patternData);
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
                for (ConfigurationSection section : getSectionList("potions.killer")) {
                    killerPotions.add(new PotionData(
                            section.getString("type"),
                            section.getInt("amplifier", 1),
                            section.getInt("duration", 100)
                    ));
                }
            }

            if (config.contains("potions.nearby")) {
                ConfigurationSection nearby = config.getConfigurationSection("potions.nearby");
                if (nearby != null) {
                    potionRadius = nearby.getDouble("radius", 5.0);
                    for (ConfigurationSection section : getSectionList(nearby, "effects")) {
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

    private void loadProjectiles() {
        projectilesEnabled = config.getBoolean("projectiles.enabled", false);
        projectiles = new ArrayList<>();

        if (projectilesEnabled && config.contains("projectiles.launch")) {
            for (ConfigurationSection section : getSectionList("projectiles.launch")) {
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

    private void loadBlocks() {
        blocksEnabled = config.getBoolean("blocks.enabled", false);
        blocksTemporary = config.getBoolean("blocks.temporary", true);
        blocksRestoreDelay = config.getInt("blocks.restore_delay", 100);
        blockPlacements = new ArrayList<>();

        if (blocksEnabled && config.contains("blocks.place")) {
            for (ConfigurationSection section : getSectionList("blocks.place")) {
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

    private void loadEntities() {
        entitiesEnabled = config.getBoolean("entities.enabled", false);
        entities = new ArrayList<>();

        if (entitiesEnabled && config.contains("entities.spawn")) {
            for (ConfigurationSection section : getSectionList("entities.spawn")) {
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

    private List<String> loadAliases() {
        Set<String> collectedAliases = new LinkedHashSet<>();
        collectedAliases.addAll(normalizeEffectIds(config.getStringList("effect.aliases")));
        collectedAliases.addAll(normalizeEffectIds(config.getStringList("effect.legacy_ids")));

        String baseFileName = fileName;
        if (baseFileName != null && baseFileName.toLowerCase(Locale.ROOT).endsWith(".yml")) {
            baseFileName = baseFileName.substring(0, baseFileName.length() - 4);
        }

        String normalizedFileName = normalizeEffectId(baseFileName);
        if (!normalizedFileName.isEmpty() && !normalizedFileName.equals(id)) {
            collectedAliases.add(normalizedFileName);
        }

        collectedAliases.remove(id);
        return new ArrayList<>(collectedAliases);
    }

    private List<ConfigurationSection> getSectionList(String path) {
        return getSectionList(config, path);
    }

    private List<ConfigurationSection> getSectionList(ConfigurationSection root, String path) {
        List<?> rawList = root.getList(path);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        List<ConfigurationSection> sections = new ArrayList<>();
        for (Object obj : rawList) {
            ConfigurationSection section = toSection(obj);
            if (section != null) {
                sections.add(section);
            }
        }
        return sections;
    }

    private ConfigurationSection toSection(Object obj) {
        if (obj instanceof ConfigurationSection section) {
            return section;
        }

        if (obj instanceof Map<?, ?> map) {
            YamlConfiguration tempConfig = new YamlConfiguration();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                tempConfig.set(String.valueOf(entry.getKey()), entry.getValue());
            }
            return tempConfig;
        }

        return null;
    }

    private void applyColorSettings(ConfigurationSection section, ColorConfigurable configurable) {
        ConfigurationSection color = section.getConfigurationSection("color");
        if (color == null) {
            return;
        }

        configurable.setColor(
                color.getInt("red", 255),
                color.getInt("green", 255),
                color.getInt("blue", 255)
        );
        configurable.setSize(section.getDouble("size", 1.0));
    }

    private List<String> normalizeEffectIds(List<String> rawValues) {
        List<String> normalizedValues = new ArrayList<>();
        for (String rawValue : rawValues) {
            String normalized = normalizeEffectId(rawValue);
            if (!normalized.isEmpty()) {
                normalizedValues.add(normalized);
            }
        }
        return normalizedValues;
    }

    private String normalizeEffectId(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public String getId() { return id; }
    public List<String> getAliases() { return aliases; }
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
                if (sound == null || sound.isBlank()) {
                    return null;
                }
                NamespacedKey key = sound.contains(":")
                        ? NamespacedKey.fromString(sound.toLowerCase())
                        : NamespacedKey.minecraft(sound.toLowerCase().replace('_', '.'));
                if (key == null) {
                    return null;
                }
                return Registry.SOUNDS.get(key);
            } catch (Exception e) {
                return null;
            }
        }

        public double getVolume() { return volume; }
        public double getPitch() { return pitch; }
        public int getDelay() { return delay; }
    }

    public interface ColorConfigurable {
        void setColor(int red, int green, int blue);
        void setSize(double size);
    }

    public static class ParticleData implements ColorConfigurable {
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

        @Override
        public void setColor(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
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

    public static class PatternData implements ColorConfigurable {
        private final String type;
        private final String particle;
        private final double radius;
        private final int points;
        private final int duration;
        private final double speed;
        private final double height;
        private int red = 255;
        private int green = 255;
        private int blue = 255;
        private double size = 1.0;

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
        @Override
        public void setColor(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
        @Override
        public void setSize(double size) {
            this.size = size;
        }
        public double getRadius() { return radius; }
        public int getPoints() { return points; }
        public int getDuration() { return duration; }
        public double getSpeed() { return speed; }
        public double getHeight() { return height; }
        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
        public double getSize() { return size; }
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
            return PotionEffectUtils.fromName(type);
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
