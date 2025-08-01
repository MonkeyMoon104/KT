package com.monkey.kt;

import com.monkey.kt.commands.kt.KTCommand;
import com.monkey.kt.commands.kt.subcommands.list.KillCoinsCommand;
import com.monkey.kt.commands.kt.tab.KillEffectTabCompleter;
import com.monkey.kt.config.ConfigService;
import com.monkey.kt.cooldown.CooldownManager;
import com.monkey.kt.economy.KillCoinsEco;
import com.monkey.kt.economy.storage.KillCoinsDatabaseManager;
import com.monkey.kt.economy.storage.KillCoinsStorage;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.effects.register.EffectRegistry;
import com.monkey.kt.listener.*;
import com.monkey.kt.storage.DatabaseManager;
import com.monkey.kt.gui.GUIManager;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.KTStatusLogger;
import com.monkey.kt.utils.WorldGuardUtils;
import com.monkey.kt.utils.listener.CheckUpdate;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class KT extends JavaPlugin {

    private DatabaseManager databaseManager;
    private GUIManager guiManager;
    private KillEffectFactory factory;
    private EffectRegistry effectRegistry;
    private KillCoinsDatabaseManager coinsDbManager;
    private KillCoinsEco killCoinsEco;
    private CooldownManager cooldownManager;
    private KTStatusLogger statusLogger;

    @Override
    public void onEnable() {
        new Metrics(this, 26511);

        int spigotResourceId = 125998;
        CheckUpdate checkUpdate = new CheckUpdate(this, spigotResourceId);
        getServer().getPluginManager().registerEvents(checkUpdate, this);

        saveDefaultConfig();

        new ConfigService(this).updateAndReload();

        //loadResourcePack(); <-- Temporary disabilited

        databaseManager = new DatabaseManager(this);
        databaseManager.loadDatabase();

        coinsDbManager = new KillCoinsDatabaseManager(this);
        coinsDbManager.loadDatabase();

        KillCoinsStorage storage = new KillCoinsStorage();
        killCoinsEco = new KillCoinsEco(this, storage);

        WorldGuardUtils.setup();

        guiManager = new GUIManager(this, killCoinsEco);
        effectRegistry = new EffectRegistry(this);
        effectRegistry.loadEffects();

        statusLogger = new KTStatusLogger(this, 26511);
        statusLogger.logEnable();

        TempBlockStorage.removeAllTempBlocks();

        getCommand("killeffect").setExecutor(new KTCommand(this, guiManager, killCoinsEco));
        KillCoinsCommand killCoinsCmd = new KillCoinsCommand(this, killCoinsEco);
        getCommand("killeffect").setTabCompleter(new KillEffectTabCompleter(this, killCoinsCmd));

        //getServer().getPluginManager().registerEvents(new ResourcePackListenerJoin(this), this); <-- Temporary disabilited
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this, killCoinsEco), this);
        getServer().getPluginManager().registerEvents(new ArrowDamageTracker(this), this);
        getServer().getPluginManager().registerEvents(new KillEffectListener(this), this);
        getServer().getPluginManager().registerEvents(new WitherSkullProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new EntityByPassSpawn(), this);
        getServer().getPluginManager().registerEvents(new KillRewardListener(this, killCoinsEco), this);

    }

    @Override
    public void onDisable() {
        TempBlockStorage.removeAllTempBlocks();
        databaseManager.closeConnection();
        if (coinsDbManager != null) coinsDbManager.close();
    }

    private void loadResourcePack() {
        String url = getConfig().getString("resource_pack.url");
        String sha = getConfig().getString("resource_pack.sha1");

        if (url == null || sha == null) {
            getLogger().warning("Resource pack URL o SHA1 not found in config.yml!");
            return;
        }

        if (!sha.matches("^[a-fA-F0-9]{40}$")) {
            getLogger().warning("SHA1 not valid! Need to be long max 40 characters.");
            return;
        }

        getLogger().info("Resource pack configured: " + url);
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
    public KillCoinsEco getKillCoinsEco() {
        return killCoinsEco;
    }

}
