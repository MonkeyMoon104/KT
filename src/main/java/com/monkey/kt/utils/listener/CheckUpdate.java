package com.monkey.kt.utils.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckUpdate implements Listener {

    private final Plugin plugin;
    private final int resourceId;
    private String latestVersion;
    private boolean updateAvailable;

    public CheckUpdate(Plugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.latestVersion = "";
        this.updateAvailable = false;

        new BukkitRunnable() {
            @Override
            public void run() {
                checkForUpdates();
            }
        }.runTaskAsynchronously(plugin);
    }

    private void checkForUpdates() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String latestVersion = reader.readLine();
            reader.close();

            this.latestVersion = latestVersion;

            String currentVersion = plugin.getDescription().getVersion();

            updateAvailable = !currentVersion.equalsIgnoreCase(latestVersion);

            if (updateAvailable) {
                Bukkit.getLogger().info("[KT UpdateChecker] New version available: " + latestVersion + " (current version: " + currentVersion + ")");
            } else {
                Bukkit.getLogger().info("[KT UpdateChecker] Plugin update to last version");
            }

        } catch (Exception e) {
            Bukkit.getLogger().warning("[KT UpdateChecker] Error while check updates: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (updateAvailable && player.isOp()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.sendMessage("§6[KT UpdateChecker] §cNew version of plugin available §eVersion: " + latestVersion);
                        player.sendMessage("§6[KT UpdateChecker] §aDownload it here: §bhttps://www.spigotmc.org/resources/" + resourceId);
                    }
                }
            }.runTaskLater(plugin, 20L * 10);
        }
    }
}
