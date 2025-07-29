package com.monkey.kt.utils.update;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class GitHubUpdater {

    private final Plugin plugin;
    private final String apiUrl = "https://api.github.com/repos/MonkeyMoon104/KT/releases/latest";

    public GitHubUpdater(Plugin plugin) {
        this.plugin = plugin;
    }

    public void checkAndUpdate() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) json.append(line);
            in.close();

            String jsonStr = json.toString();

            String latestVersion = extract(jsonStr, "\"tag_name\":\"", "\"");
            String currentVersion = plugin.getDescription().getVersion();

            if (!latestVersion.equalsIgnoreCase("v" + currentVersion)) {
                Bukkit.getLogger().info("[KT AutoUpdater] New version found: " + latestVersion);
                String downloadUrl = findJarDownloadUrl(jsonStr);
                if (downloadUrl == null) {
                    Bukkit.getLogger().warning("[KT AutoUpdater] No .jar asset found in release.");
                    return;
                }

                File updateDir = new File("plugins/update");
                if (!updateDir.exists()) updateDir.mkdirs();

                File destination = new File(updateDir, "KT-" + latestVersion + ".jar");
                downloadFile(downloadUrl, destination);

                Bukkit.getLogger().info("[KT AutoUpdater] Download complete. Update saved in /plugins/update. Please restart the server to apply the update.");
            } else {
                Bukkit.getLogger().info("[KT AutoUpdater] Plugin is up to date.");
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[KT AutoUpdater] Update check failed: " + e.getMessage());
        }
    }

    private String extract(String source, String start, String end) {
        int s = source.indexOf(start);
        if (s == -1) return null;
        s += start.length();
        int e = source.indexOf(end, s);
        if (e == -1) return null;
        return source.substring(s, e);
    }

    private String findJarDownloadUrl(String json) {
        String[] parts = json.split("\\{");
        for (String part : parts) {
            if (part.contains("\"browser_download_url\"") && part.contains(".jar")) {
                String url = extract(part, "\"browser_download_url\":\"", "\"");
                if (url != null && url.endsWith(".jar")) {
                    return url;
                }
            }
        }
        return null;
    }

    private void downloadFile(String fileURL, File destination) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(fileURL).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, destination.toPath());
        }
    }
}
