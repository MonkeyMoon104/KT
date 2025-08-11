package com.monkey.kt.utils.registration;

import com.monkey.kt.KT;
import com.monkey.kt.utils.discord.WebhookManager;
import com.monkey.kt.utils.discord.security.WebhookDecryptor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistrationManager {

    private final KT plugin;
    private File registrationFile;
    private FileConfiguration registrationConfig;
    private WebhookManager webhookManager;

    public RegistrationManager(KT plugin) {
        this.plugin = plugin;

        String webhookUrl = WebhookDecryptor.getRegistrationWebhook();
        if (webhookUrl != null) {
            this.webhookManager = new WebhookManager(webhookUrl, plugin);
        }
    }

    public void setup() {
        File registrationFolder = new File(plugin.getDataFolder(), "registration");
        if (!registrationFolder.exists()) {
            registrationFolder.mkdirs();
        }

        registrationFile = new File(registrationFolder, "registration.yml");

        if (!registrationFile.exists()) {
            try (InputStream in = plugin.getResource("registration.yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, registrationFile.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        registrationConfig = YamlConfiguration.loadConfiguration(registrationFile);

        boolean isFirstTime = registrationConfig.getString("first-registration", "").isEmpty();

        registrationConfig.set("current-version", plugin.getDescription().getVersion());
        registrationConfig.set("server-name", Bukkit.getServer().getName());
        registrationConfig.set("server-ip", Bukkit.getIp().isEmpty() ? getLocalIP() : Bukkit.getIp());
        registrationConfig.set("server-port", Bukkit.getPort());
        registrationConfig.set("bukkit-version", Bukkit.getVersion());

        if (isFirstTime) {
            registrationConfig.set("first-registration", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            if (webhookManager == null) {
                plugin.getLogger().warning("registration url failed!");
                return;
            }

            webhookManager.sendRegistrationWebhook(
                    registrationConfig.getString("first-registration"),
                    plugin.getDescription().getVersion(),
                    Bukkit.getServer().getName(),
                    Bukkit.getIp().isEmpty() ? getLocalIP() : Bukkit.getIp(),
                    Bukkit.getPort(),
                    Bukkit.getVersion()
            );
        }

        save();
    }

    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public void save() {
        try {
            registrationConfig.save(registrationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
