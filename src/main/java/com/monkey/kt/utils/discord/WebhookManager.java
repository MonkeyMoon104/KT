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

    private final String webhookUrl;
    private final KT plugin;

    public WebhookManager(String webhookUrl, KT plugin) {
        this.webhookUrl = webhookUrl;
        this.plugin = plugin;
    }

    public void sendSuggestion(String playerName, String suggestion, boolean isBug) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String mentionMsg = isBug
                        ? "<@1203449110460506252> 🐛 **Nuovo bug report ricevuto!**"
                        : "<@1203449110460506252> 💡 **Nuova suggestion ricevuta!**";

                sendWebhookMessage("{\"content\": \"" + escapeJson(mentionMsg) + "\"}");

                String serverIp = Bukkit.getIp();
                int port = Bukkit.getPort();
                String ipDisplay = serverIp.isEmpty() ? "localhost" : serverIp;
                ipDisplay += ":" + port;

                String authorBlock = "\"name\": \"" + escapeJson(playerName) + "\"";
                if (playerName != null && !playerName.trim().isEmpty()) {
                    try {
                        authorBlock += ",\"icon_url\": \"https://minotar.net/avatar/" + escapeJson(playerName) + "/64\"";
                    } catch (Exception ignored) {}
                }

                String title = isBug ? "🐛 Nuovo Bug Report" : "💡 Nuova Suggestion";
                String descriptionPrefix = isBug ? "> 🚨 **Descrizione bug:**\n" : "> 📜 **Suggerimento:**\n";
                int color = isBug ? 16711680 : 16755200;

                StringBuilder fields = new StringBuilder();
                fields.append("{ \"name\": \"🖥️ Server\", \"value\": \"")
                        .append(escapeJson("**" + Bukkit.getServer().getName() + "**"))
                        .append("\", \"inline\": true },");
                fields.append("{ \"name\": \"⚙️ Versione\", \"value\": \"")
                        .append(escapeJson("`" + Bukkit.getVersion() + "`"))
                        .append("\", \"inline\": true },");
                fields.append("{ \"name\": \"🌐 IP\", \"value\": \"")
                        .append(escapeJson("`" + ipDisplay + "`"))
                        .append("\", \"inline\": true }");

                if (plugin.getConfig().getBoolean("server-info", false)) {
                    int onlinePlayers = Bukkit.getOnlinePlayers().size();
                    int maxPlayers = Bukkit.getMaxPlayers();

                    fields.append(",{ \"name\": \"👥 Giocatori Online\", \"value\": \"")
                            .append(escapeJson(onlinePlayers + "/" + maxPlayers))
                            .append("\", \"inline\": true }");

                    String playerList;
                    if (onlinePlayers > 30) {
                        playerList = Bukkit.getOnlinePlayers().stream()
                                .limit(30)
                                .map(Player::getName)
                                .collect(Collectors.joining(", "))
                                + " ... e altri " + (onlinePlayers - 30);
                    } else {
                        playerList = Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .collect(Collectors.joining(", "));
                    }
                    fields.append(",{ \"name\": \"📋 Lista Giocatori\", \"value\": \"")
                            .append(escapeJson(playerList.isEmpty() ? "Nessuno" : playerList))
                            .append("\", \"inline\": false }");

                    double avgPing = Bukkit.getOnlinePlayers().stream()
                            .mapToInt(Player::getPing)
                            .average()
                            .orElse(0);
                    fields.append(",{ \"name\": \"📡 Ping Medio\", \"value\": \"")
                            .append(escapeJson(Math.round(avgPing) + " ms"))
                            .append("\", \"inline\": true }");
                }

                String jsonPayload = "{"
                        + "\"embeds\": [{"
                        + "\"title\": \"" + title + "\","
                        + "\"description\": \"" + escapeJson(descriptionPrefix + suggestion) + "\","
                        + "\"color\": " + color + ","
                        + "\"timestamp\": \"" + Instant.now().toString() + "\","
                        + "\"author\": {" + authorBlock + "},"
                        + "\"fields\": [" + fields + "],"
                        + "\"footer\": {"
                        + "\"text\": \"Sistema segnalazioni • Powered by KT\","
                        + "\"icon_url\": \"https://i.imgur.com/AfFp7pu.png\""
                        + "}"
                        + "}]"
                        + "}";

                sendWebhookMessage(jsonPayload);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendReview(String playerName, int stars) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < stars; i++) {
                    sb.append("⭐");
                }
                String starsEmoji = sb.toString();

                long nextReviewTime = Instant.now().getEpochSecond() + 86400;
                String nextReviewTimestamp = "<t:" + nextReviewTime + ":R>";

                String authorBlock = "\"name\": \"" + escapeJson(playerName) + "\"";
                if (playerName != null && !playerName.trim().isEmpty()) {
                    try {
                        authorBlock += ",\"icon_url\": \"https://minotar.net/avatar/" + escapeJson(playerName) + "/64\"";
                    } catch (Exception ignored) {}
                }

                String description = escapeJson(starsEmoji + "\n\n⏰ The player can review again " + nextReviewTimestamp);

                String jsonPayload = "{"
                        + "\"embeds\":[{"
                        + "\"title\":\"📝 New Review - In Game\","
                        + "\"description\":\"" + description + "\","
                        + "\"color\": 3447003,"
                        + "\"timestamp\":\"" + Instant.now().toString() + "\","
                        + "\"author\":{" + authorBlock + "},"
                        + "\"footer\":{"
                        + "\"text\":\"Plugin Reviews • Powered by KT\","
                        + "\"icon_url\":\"https://i.imgur.com/AfFp7pu.png\""
                        + "}"
                        + "}]"
                        + "}";
                sendWebhookMessage(jsonPayload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendWebhookMessage(String jsonPayload) throws Exception {
        URL url = new URL(webhookUrl);
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
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
