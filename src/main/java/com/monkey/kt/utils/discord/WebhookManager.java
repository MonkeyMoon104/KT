package com.monkey.kt.utils.discord;

import com.monkey.kt.KT;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Collectors;

public class WebhookManager {

    private final KT plugin;
    private final String baseUrl = "https://kt.dominikhun250.dev/webhook";

    public WebhookManager(KT plugin) {
        this.plugin = plugin;
    }

    public void sendSuggestion(String playerName, String suggestion, boolean isBug) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String serverIp = Bukkit.getIp();
                int port = Bukkit.getPort();
                String ipDisplay = serverIp.isEmpty() ? "localhost" : serverIp;

                int onlinePlayers = Bukkit.getOnlinePlayers().size();
                int maxPlayers = Bukkit.getMaxPlayers();

                String playerList;
                if (onlinePlayers > 30) {
                    playerList = Bukkit.getOnlinePlayers().stream()
                            .limit(30)
                            .map(Player::getName)
                            .collect(Collectors.joining(", "))
                            + " ..." + (onlinePlayers - 30);
                } else {
                    playerList = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.joining(", "));
                }

                double avgPing = Bukkit.getOnlinePlayers().stream()
                        .mapToInt(Player::getPing)
                        .average()
                        .orElse(0);

                String jsonPayload = "{"
                        + "\"playerName\":\"" + escapeJson(playerName) + "\","
                        + "\"suggestion\":\"" + escapeJson(suggestion) + "\","
                        + "\"isBug\":" + isBug + ","
                        + "\"serverName\":\"" + escapeJson(Bukkit.getServer().getName()) + "\","
                        + "\"serverVersion\":\"" + escapeJson(Bukkit.getVersion()) + "\","
                        + "\"serverIp\":\"" + escapeJson(ipDisplay) + "\","
                        + "\"serverPort\":" + port + ","
                        + "\"onlinePlayers\":" + onlinePlayers + ","
                        + "\"maxPlayers\":" + maxPlayers + ","
                        + "\"playerList\":\"" + escapeJson(playerList) + "\","
                        + "\"avgPing\":" + Math.round(avgPing)
                        + "}";

                sendWebhookMessage(baseUrl + "/suggest", jsonPayload);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendReview(String playerName, int stars, String comment, String contact) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String jsonPayload = "{"
                        + "\"playerName\":\"" + escapeJson(playerName) + "\","
                        + "\"stars\":" + stars + ","
                        + "\"comment\":\"" + escapeJson(comment == null ? "" : comment) + "\","
                        + "\"contact\":\"" + escapeJson(contact == null ? "" : contact) + "\""
                        + "}";

                sendWebhookMessage(baseUrl + "/review", jsonPayload);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendRegistrationWebhook(String firstRegistration, String currentVersion, String serverName,
                                        String serverIp, int serverPort, String bukkitVersion) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String jsonPayload = "{"
                        + "\"firstRegistration\":\"" + escapeJson(firstRegistration) + "\","
                        + "\"currentVersion\":\"" + escapeJson(currentVersion) + "\","
                        + "\"serverName\":\"" + escapeJson(serverName) + "\","
                        + "\"serverIp\":\"" + escapeJson(serverIp) + "\","
                        + "\"serverPort\":" + serverPort + ","
                        + "\"bukkitVersion\":\"" + escapeJson(bukkitVersion) + "\""
                        + "}";

                sendWebhookMessage(baseUrl + "/registration", jsonPayload);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendWebhookMessage(String urlStr, String jsonPayload) throws Exception {
        URL url = new URL(urlStr);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }
        connection.getInputStream().close();
        connection.disconnect();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}