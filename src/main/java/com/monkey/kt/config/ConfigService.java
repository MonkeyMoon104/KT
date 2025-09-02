package com.monkey.kt.config;

import com.monkey.kt.KT;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigService {

    private final KT plugin;
    private final String fileName;
    private final String versionKey;

    public ConfigService(KT plugin) {
        this(plugin, "config.yml", "config-version");
    }

    public ConfigService(KT plugin, String fileName, String versionKey) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.versionKey = versionKey;
    }

    public void updateAndReload() {
        try {
            plugin.saveDefaultConfig();

            File file = new File(plugin.getDataFolder(), fileName);
            File defaultFile = extractDefaultConfig();

            String mergedYaml = mergePreservingSpacing(file, defaultFile);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(mergedYaml);
            }

            Files.deleteIfExists(defaultFile.toPath());

            plugin.reloadConfig();
        } catch (Exception e) {
            plugin.getLogger().severe("Error during config update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private File extractDefaultConfig() throws Exception {
        File dest = new File(plugin.getDataFolder(), "dconfig.yml");
        if (!dest.exists()) {
            try (InputStream in = plugin.getResource("config.yml")) {
                if (in == null) throw new Exception("Default config.yml not found in jar.");
                Files.copy(in, dest.toPath());
            }
        }
        return dest;
    }
    private String mergePreservingSpacing(File userFile, File defaultFile) throws Exception {
        List<String> defaultLines = Files.readAllLines(defaultFile.toPath());
        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultFile);

        StringBuilder merged = new StringBuilder();
        String currentPath = "";
        String[] pathStack = new String[32];

        int skipLines = 0;

        for (int i = 0; i < defaultLines.size(); i++) {
            String line = defaultLines.get(i);

            if (skipLines > 0) {
                skipLines--;
                continue;
            }

            if (line.trim().isEmpty()) {
                merged.append("\n");
                continue;
            }

            Matcher matcher = Pattern.compile("^([ \\t]*)([\\w\\-]+):").matcher(line);
            if (matcher.find()) {
                String indent = matcher.group(1);
                String key = matcher.group(2);
                int level = indent.length() / 2;
                pathStack[level] = key;

                StringBuilder pathBuilder = new StringBuilder();
                for (int j = 0; j <= level; j++) {
                    if (pathStack[j] != null) {
                        if (pathBuilder.length() > 0) pathBuilder.append(".");
                        pathBuilder.append(pathStack[j]);
                    }
                }
                currentPath = pathBuilder.toString();

                Object value;
                if (
                        currentPath.equals(versionKey) ||
                        currentPath.equals("resource_pack.updates.url") ||
                        currentPath.equals("resource_pack.updates.sha1")
                ) {
                    value = defaultConfig.get(currentPath);
                } else {
                    value = userConfig.get(currentPath);
                    if (value == null) value = defaultConfig.get(currentPath);
                }

                if (value instanceof MemorySection) {
                    merged.append(indent).append(key).append(":").append("\n");
                } else if (value instanceof List) {
                    merged.append(indent).append(key).append(":").append("\n");
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        String itemStr = item.toString();
                        if (item instanceof String) {
                            itemStr = "\"" + itemStr.replace("\"", "\\\"") + "\"";
                        }
                        merged.append(indent).append("  - ").append(itemStr).append("\n");
                    }
                    int count = 0;
                    for (int k = i + 1; k < defaultLines.size(); k++) {
                        String nextLine = defaultLines.get(k);
                        if (nextLine.trim().startsWith("- ")) {
                            count++;
                        } else {
                            break;
                        }
                    }
                    skipLines = count;
                } else if (value instanceof String) {
                    String valStr = ((String) value).replace("\n", "\\n").replace("\"", "\\\"");
                    valStr = "\"" + valStr + "\"";
                    merged.append(indent).append(key).append(": ").append(valStr).append("\n");
                } else {
                    String valStr = String.valueOf(value);
                    merged.append(indent).append(key).append(": ").append(valStr).append("\n");
                }

            } else {
                merged.append(line).append("\n");
            }
        }

        return merged.toString();
    }
}
