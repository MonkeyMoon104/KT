package com.monkey.kt;

import com.monkey.kt.commands.kt.KTCommand;
import com.monkey.kt.commands.kt.tab.KillEffectTabCompleter;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.effects.register.EffectRegistry;
import com.monkey.kt.listener.*;
import com.monkey.kt.storage.DatabaseManager;
import com.monkey.kt.gui.GUIManager;
import com.monkey.kt.utils.WorldGuardUtils;
import com.monkey.kt.utils.listener.CheckUpdate;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class KT extends JavaPlugin {

    private DatabaseManager databaseManager;
    private GUIManager guiManager;
    private KillEffectFactory factory;
    private EffectRegistry effectRegistry;

    @Override
    public void onEnable() {
        new Metrics(this, 26511);

        int spigotResourceId = 125998;
        CheckUpdate checkUpdate = new CheckUpdate(this, spigotResourceId);
        getServer().getPluginManager().registerEvents(checkUpdate, this);

        saveDefaultConfig();

        loadResourcePack();

        databaseManager = new DatabaseManager(this);
        databaseManager.loadDatabase();

        WorldGuardUtils.setup();

        guiManager = new GUIManager(this, databaseManager);
        effectRegistry = new EffectRegistry(this);
        effectRegistry.loadEffects();

        getCommand("killeffect").setExecutor(new KTCommand(this, guiManager));
        getCommand("killeffect").setTabCompleter(new KillEffectTabCompleter(this));

        getServer().getPluginManager().registerEvents(new ResourcePackListenerJoin(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ArrowDamageTracker(this), this);
        getServer().getPluginManager().registerEvents(new KillEffectListener(), this);
        getServer().getPluginManager().registerEvents(new WitherSkullProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new EntityByPassSpawn(), this);

    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
    }

    private void loadResourcePack() {
        String url = getConfig().getString("resource_pack.url");
        String sha = getConfig().getString("resource_pack.sha1");

        if (url == null || sha == null) {
            getLogger().warning("Resource pack URL o SHA1 mancante nel config.yml!");
            return;
        }

        if (!sha.matches("^[a-fA-F0-9]{40}$")) {
            getLogger().warning("SHA1 non valido! Deve essere lungo 40 caratteri hex.");
            return;
        }

        getLogger().info("Resource pack configurato: " + url);
    }


    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public EffectRegistry getEffectRegistry() {
        return effectRegistry;
    }

}
