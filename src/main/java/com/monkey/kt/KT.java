package com.monkey.kt;

import com.monkey.kt.boost.AuraBoostManager;
import com.monkey.kt.commands.kt.KTCommand;
import com.monkey.kt.commands.kt.subcommands.list.KillCoinsCommand;
import com.monkey.kt.commands.kt.tab.KillEffectTabCompleter;
import com.monkey.kt.config.ConfigService;
import com.monkey.kt.cooldown.CooldownManager;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.economy.KillCoinsEco;
import com.monkey.kt.economy.storage.KillCoinsDatabaseManager;
import com.monkey.kt.economy.storage.KillCoinsStorage;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.effects.register.EffectRegistry;
import com.monkey.kt.events.EventManager;
import com.monkey.kt.listener.*;
import com.monkey.kt.placeholder.KTPlaceholder;
import com.monkey.kt.storage.DatabaseManager;
import com.monkey.kt.gui.GUIManager;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.KTStatusLogger;
import com.monkey.kt.utils.SoundUtils;
import com.monkey.kt.utils.WorldGuardUtils;
import com.monkey.kt.utils.listener.CheckUpdate;
import com.monkey.kt.utils.registration.RegistrationManager;
import com.monkey.kt.utils.resourcepack.ResourcePack;
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
    private AuraBoostManager auraBoostManager;
    private ResourcePack resourcePack;
    private KT instance;
    private SoundUtils soundUtils;
    private EconomyManager economyManager;
    private InventoryClickListener inventoryClickListener;
    private KillRewardListener killRewardListener;
    private KTCommand ktCommand;
    private EventManager eventManager;

    @Override
    public void onEnable() {
        instance = this;
        new Metrics(this, 26511);

        int spigotResourceId = 125998;
        CheckUpdate checkUpdate = new CheckUpdate(this, spigotResourceId);
        getServer().getPluginManager().registerEvents(checkUpdate, this);

        saveDefaultConfig();

        new ConfigService(this).updateAndReload();

        this.resourcePack = new ResourcePack(this);

        loadResourcePack();

        databaseManager = new DatabaseManager(this);
        databaseManager.loadDatabase();

        coinsDbManager = new KillCoinsDatabaseManager(this);
        coinsDbManager.loadDatabase();

        KillCoinsStorage storage = new KillCoinsStorage();
        killCoinsEco = new KillCoinsEco(this, storage);

        economyManager = new EconomyManager(this, killCoinsEco);

        WorldGuardUtils.setup();

        eventManager = new EventManager(this);

        guiManager = new GUIManager(this, economyManager);
        effectRegistry = new EffectRegistry(this);
        effectRegistry.loadEffects();

        statusLogger = new KTStatusLogger(this, 26511, economyManager);
        statusLogger.logEnable();

        TempBlockStorage.removeAllTempBlocks();

        ktCommand = new KTCommand(this, guiManager, economyManager);
        getCommand("killeffect").setExecutor(ktCommand);

        KillCoinsCommand killCoinsCmd = new KillCoinsCommand(this, economyManager);
        getCommand("killeffect").setTabCompleter(new KillEffectTabCompleter(this, killCoinsCmd));

        inventoryClickListener = new InventoryClickListener(this, economyManager);
        getServer().getPluginManager().registerEvents(inventoryClickListener, this);

        getServer().getPluginManager().registerEvents(new ResourcePackListenerJoin(this), this);
        getServer().getPluginManager().registerEvents(new KillEffectDamageTracker(this), this);
        getServer().getPluginManager().registerEvents(new KillEffectListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileProtListener(), this);
        getServer().getPluginManager().registerEvents(new EntityByPassSpawn(), this);

        killRewardListener = new KillRewardListener(this, economyManager);
        getServer().getPluginManager().registerEvents(killRewardListener, this);

        this.auraBoostManager = new AuraBoostManager();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KTPlaceholder(getAuraBoostManager(), this).register();
        }

        RegistrationManager registrationManager = new RegistrationManager(this);
        registrationManager.setup();

        this.soundUtils = new SoundUtils(this);
    }

    @Override
    public void onDisable() {
        TempBlockStorage.removeAllTempBlocks();
        if (eventManager != null) {
            eventManager.stopAllEvents();
        }
        databaseManager.closeConnection();
        if (coinsDbManager != null) coinsDbManager.close();
    }

    private void loadResourcePack() {
        if (!getConfig().getBoolean("resource_pack.settings.enabled", true)) {
            getLogger().warning("ResourcePack deactivated in config");
            return;
        }

        String url = getConfig().getString("resource_pack.settings.url");
        String sha = getConfig().getString("resource_pack.settings.sha1");

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

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    public void setEconomyManager(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }
    public AuraBoostManager getAuraBoostManager() {
        return auraBoostManager;
    }
    public ResourcePack getResourcePack() {
        return resourcePack;
    }
    public SoundUtils getSoundUtils() { return soundUtils; }
    public InventoryClickListener getInventoryClickListener() {
        return inventoryClickListener;
    }
    public KillRewardListener getKillRewardListener() {
        return killRewardListener;
    }
    public KTCommand getKtCommand() {
        return ktCommand;
    }
    public KTStatusLogger getStatusLogger () {
        return statusLogger;
    }
    public EventManager getEventManager() {
        return eventManager;
    }
}
