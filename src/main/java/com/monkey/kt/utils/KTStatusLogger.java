package com.monkey.kt.utils;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.logging.Logger;

public class KTStatusLogger {

    private final KT plugin;
    private final Logger logger;
    private final int pluginId;
    private EconomyManager eco;

    private static final String RESET = "\u001B[0m";
    private static final String DARK_GRAY = "\u001B[38;2;85;85;85m";
    private static final String LIGHT_GRAY = "\u001B[38;2;180;180;180m";
    private static final String ORANGE = "\u001B[38;2;255;165;0m";
    private static final String CYAN = "\u001B[38;2;0;255;255m";
    private static final String GREEN = "\u001B[38;2;100;255;100m";
    private static final String RED = "\u001B[38;2;255;100;100m";
    private static final String PURPLE = "\u001B[38;2;190;100;255m";

    private static final int WIDTH = 55;

    private static final String SPIGOT_LINK = "https://www.spigotmc.org/resources/%E2%AD%90-1-13-1-21-killeffects-%E2%AD%90.125998/";
    private static final String GITHUB_LINK = "https://github.com/MonkeyMoon104/KT/releases";

    public KTStatusLogger(KT plugin, int pluginId, EconomyManager eco) {
        this.plugin = plugin;
        this.pluginId = pluginId;
        this.logger = plugin.getLogger();
        this.eco = eco;
    }

    public void updateEconomyManager(EconomyManager economyManager) {
        this.eco = economyManager;
        plugin.getLogger().info("StatusLogger updated with new EconomyManager: " +
                (economyManager.isUsingInternal() ? "Internal" : "External"));
    }

    public void logEnable() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int servers = fetchServersUsingPlugin();
            int players = fetchPlayersUsingPlugin();

            plugin.getServer().getScheduler().runTask(plugin, () -> logWithStats("KT Plugin Enabled", servers, players));
        });
    }

    public void logReload() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int servers = fetchServersUsingPlugin();
            int players = fetchPlayersUsingPlugin();

            plugin.getServer().getScheduler().runTask(plugin, () -> logWithStats("KT Plugin Reloaded", servers, players));
        });
    }

    private void logWithStats(String title, int serversUsing, int playersUsing) {
        String pluginName = plugin.getDescription().getName();
        String version = plugin.getDescription().getVersion();
        String author = getAuthor();

        int effectsCount = 0;
        if (plugin.getEffectRegistry() != null) {
            try {
                effectsCount = plugin.getEffectRegistry().getLoadedEffectsCount();
            } catch (Exception e) {
                logger.warning("Error while retrieving loaded effects count.");
            }
        }

        boolean dbOpen = false;
        try {
            if (plugin.getDatabaseManager() != null) {
                Connection conn = plugin.getDatabaseManager().getConnection();
                dbOpen = (conn != null && !conn.isClosed());
            }
        } catch (Exception e) {
            logger.warning("Error while checking database status.");
        }

        boolean econActive = false;
        String econType = "Unknown";
        try {
            if (eco != null) {
                econActive = eco.isEnabled();
                if (eco.isUsingInternal()) {
                    econType = "Internal (KillCoins)";
                } else if (eco.getVaultEconomy() != null) {
                    econType = "External (" + eco.getVaultEconomy().getName() + ")";
                } else {
                    econType = "External (Vault - Fallback to Internal)";
                }
            }
        } catch (Exception e) {
            logger.warning("Error while checking economy status.");
        }

        String resourcePackUrl = null;
        boolean hasResourcePack = false;

        try {
            boolean enabled = plugin.getConfig().getBoolean("resource_pack.settings.enabled", true);
            resourcePackUrl = plugin.getConfig().getString("resource_pack.settings.url");
            hasResourcePack = enabled && resourcePackUrl != null && !resourcePackUrl.isEmpty();
        } catch (Exception e) {
            hasResourcePack = false;
        }

        logger.info(DARK_GRAY + "╔" + repeat("═", WIDTH) + "╗" + RESET);
        logger.info(DARK_GRAY + "║" + repeat(" ", (WIDTH - title.length()) / 2) + ORANGE + title + DARK_GRAY + repeat(" ", WIDTH - ((WIDTH - title.length()) / 2) - title.length()) + "║" + RESET);
        logger.info(DARK_GRAY + "╠" + repeat("═", WIDTH) + "╣" + RESET);

        logger.info(ORANGE + "  Version: " + LIGHT_GRAY + pluginName + " " + version + RESET);
        logger.info(ORANGE + "  Author: " + LIGHT_GRAY + author + RESET);
        logger.info(ORANGE + "  Effects loaded: " + CYAN + effectsCount + RESET);
        logger.info(ORANGE + "  Database: " + (dbOpen ? GREEN + "Connected" : RED + "Closed") + RESET);
        logger.info(ORANGE + "  Economy: " + (econActive ? GREEN + "Enabled" : RED + "Disabled") + " " + CYAN + "(" + econType + ")" + RESET);
        logger.info(ORANGE + "  Resource Pack: " + (hasResourcePack ? CYAN + resourcePackUrl : RED + "Not configured") + RESET);

        logger.info(ORANGE + "  Servers using KT: " + CYAN + (serversUsing >= 0 ? serversUsing : "Unknown") + RESET);
        logger.info(ORANGE + "  Players using KT: " + CYAN + (playersUsing >= 0 ? playersUsing : "Unknown") + RESET);

        logger.info(ORANGE + "  SpigotMC: " + CYAN + SPIGOT_LINK + RESET);
        logger.info(ORANGE + "  GitHub: " + CYAN + GITHUB_LINK + RESET);

        logger.info(DARK_GRAY + "╚" + repeat("═", WIDTH) + "╝" + RESET);

        logger.info(PURPLE + centerText("Enjoy your game! :)", WIDTH) + RESET + "\n");
    }

    private int fetchServersUsingPlugin() {
        try {
            String json = readUrl("https://bstats.org/api/v1/plugins/" + pluginId + "/charts/servers/data");
            JSONArray data = new JSONArray(json);
            if (data.length() > 0) {
                JSONArray lastEntry = data.getJSONArray(data.length() - 1);
                return lastEntry.getInt(1);
            }
        } catch (Exception e) {
            logger.warning("Failed to fetch server data from bStats: " + e.getMessage());
        }
        return -1;
    }

    private int fetchPlayersUsingPlugin() {
        try {
            String json = readUrl("https://bstats.org/api/v1/plugins/" + pluginId + "/charts/players/data");
            JSONArray data = new JSONArray(json);
            if (data.length() > 0) {
                JSONArray lastEntry = data.getJSONArray(data.length() - 1);
                return lastEntry.getInt(1);
            }
        } catch (Exception e) {
            logger.warning("Failed to fetch player data from bStats: " + e.getMessage());
        }
        return -1;
    }

    private String readUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private String getAuthor() {
        if (plugin.getDescription().getAuthors() == null || plugin.getDescription().getAuthors().isEmpty()) {
            return "Unknown";
        }
        return plugin.getDescription().getAuthors().get(0);
    }

    private String repeat(String s, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(s);
        }
        return builder.toString();
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        return repeat(" ", leftPadding) + text + repeat(" ", rightPadding);
    }
}