package com.monkey.kt.config.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public final class YamlMergeUtil {

    private YamlMergeUtil() {}

    public static int sync(FileConfiguration target, FileConfiguration defaults, String... ignoredRoots) {
        Set<String> ignored = new HashSet<>(Arrays.asList(ignoredRoots));
        int changes = 0;

        for (String key : defaults.getKeys(false)) {
            if (ignored.contains(key)) continue;
            changes += mergeSection(target, defaults, key);
        }

        for (String key : new HashSet<>(target.getKeys(false))) {
            if (ignored.contains(key)) continue;
            if (!defaults.contains(key)) {
                target.set(key, null);
                changes++;
            } else {
                changes += cleanExtraKeys(target.getConfigurationSection(key), defaults.getConfigurationSection(key));
            }
        }

        return changes;
    }

    private static int mergeSection(ConfigurationSection targetParent, ConfigurationSection defaultParent, String key) {
        int added = 0;
        Object defObj = defaultParent.get(key);
        boolean defIsSection = defObj instanceof ConfigurationSection;

        if (!targetParent.contains(key)) {
            targetParent.set(key, defObj);
            return countAllKeys(defObj);
        }

        if (defIsSection) {
            ConfigurationSection targetSec = targetParent.getConfigurationSection(key);
            ConfigurationSection defSec = defaultParent.getConfigurationSection(key);
            if (targetSec != null && defSec != null) {
                for (String child : defSec.getKeys(false)) {
                    added += mergeSection(targetSec, defSec, child);
                }
            }
        }
        return added;
    }

    private static int cleanExtraKeys(ConfigurationSection target, ConfigurationSection defaults) {
        if (target == null || defaults == null) return 0;
        int removed = 0;
        for (String key : new HashSet<>(target.getKeys(false))) {
            if (!defaults.contains(key)) {
                target.set(key, null);
                removed++;
            } else {
                removed += cleanExtraKeys(target.getConfigurationSection(key), defaults.getConfigurationSection(key));
            }
        }
        return removed;
    }

    private static int countAllKeys(Object obj) {
        if (!(obj instanceof ConfigurationSection)) return 1;
        ConfigurationSection sec = (ConfigurationSection) obj;
        int count = 0;
        for (String key : sec.getKeys(false)) {
            Object child = sec.get(key);
            count += (child instanceof ConfigurationSection) ? countAllKeys(child) : 1;
        }
        return count;
    }

    public static FileConfiguration mergeWithOrder(FileConfiguration user, FileConfiguration defs, String versionKey) {
        YamlConfiguration result = new YamlConfiguration();

        if (defs.contains(versionKey)) {
            result.set(versionKey, user.contains(versionKey) ? user.get(versionKey) : defs.get(versionKey));
        }

        Set<String> ignored = new HashSet<>();
        ignored.add(versionKey);
        copySectionWithOrder(result, defs, user, "", ignored);

        return result;
    }

    public static String mergeWithOrderWithSpacing(FileConfiguration user, FileConfiguration defs, String versionKey) {
        FileConfiguration merged = mergeWithOrder(user, defs, versionKey);
        String rawYaml = merged.saveToString();
        String rawDefs = defs.saveToString();

        Set<String> keysWithBlank = detectBlankLinesAfterKeys(rawDefs);

        String[] lines = rawYaml.split("\n");
        StringBuilder finalYaml = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            finalYaml.append(line).append("\n");

            if (line.matches("^\\s*[^\\s#].*:\\s*$")) {
                String trimmed = line.trim();
                String key = trimmed.substring(0, trimmed.length() - 1).trim();
                int indent = line.indexOf(trimmed.charAt(0));
                String lookupKey = indent + ":" + key;

                if (keysWithBlank.contains(lookupKey)) {
                    if (i + 1 < lines.length && !lines[i + 1].trim().isEmpty()) {
                        finalYaml.append("\n");
                    }
                }
            }
        }

        return finalYaml.toString();
    }


    public static Set<String> detectBlankLinesAfterKeys(String rawYaml) {
        Set<String> keysWithBlank = new HashSet<>();
        String[] lines = rawYaml.split("\n");

        for (int i = 0; i < lines.length - 1; i++) {
            String line = lines[i];
            String nextLine = lines[i + 1];

            if (line.matches("^\\s*[^\\s#].*:\\s*$") && nextLine.trim().isEmpty()) {
                String trimmed = line.trim();
                String key = trimmed.substring(0, trimmed.length() - 1).trim();

                int indent = line.indexOf(trimmed.charAt(0));
                keysWithBlank.add(indent + ":" + key);
            }
        }

        return keysWithBlank;
    }


    private static void copySectionWithOrder(ConfigurationSection target, ConfigurationSection defs,
                                             ConfigurationSection user, String path, Set<String> ignored) {
        for (String key : defs.getKeys(false)) {
            if (ignored.contains(key)) continue;

            Object defValue = defs.get(key);
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (defValue instanceof ConfigurationSection) {
                ConfigurationSection targetChild = target.createSection(key);
                ConfigurationSection defChild = defs.getConfigurationSection(key);
                ConfigurationSection userChild = user.getConfigurationSection(key);
                if (defChild != null) {
                    copySectionWithOrder(targetChild, defChild, userChild != null ? userChild : new YamlConfiguration(), fullPath, ignored);
                }
            } else {
                Object userValue = user.contains(key) ? user.get(key) : defValue;
                target.set(key, userValue);
            }
        }
    }
}
