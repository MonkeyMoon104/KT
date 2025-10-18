package com.monkey.kt;

import com.monkey.kt.boost.AuraBoostManager;
import com.monkey.kt.commands.kt.KTCommand;
import com.monkey.kt.commands.kt.subcommands.list.KillCoinsCommand;
import com.monkey.kt.commands.kt.tab.KillEffectTabCompleter;
import com.monkey.kt.config.ConfigService;
import com.monkey.kt.cooldown.CooldownManager;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.economy.KillCoinsEco;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.effects.custom.CustomEffectLoader;
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
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class KT extends JavaPlugin {

    private DatabaseManager databaseManager;
    private GUIManager guiManager;
    private KillEffectFactory factory;
    private EffectRegistry effectRegistry;
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
    private CustomEffectLoader customEffectLoader;

    @Override
    public void onEnable() {
        instance = this;

        SchedulerWrapper.initialize();

        if (SchedulerWrapper.isFolia()) {
            getLogger().info("Detected Folia - using Folia schedulers");
        } else {
            getLogger().info("Using standard Bukkit schedulers");
        }

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

        economyManager = new EconomyManager(this, databaseManager);
        economyManager.initialize();

        WorldGuardUtils.setup();

        eventManager = new EventManager(this);

        effectRegistry = new EffectRegistry(this);
        effectRegistry.loadEffects(false);

        this.customEffectLoader = new CustomEffectLoader(this);
        this.customEffectLoader.loadAllCustomEffects();

        cleanupObsoleteEffects();

        guiManager = new GUIManager(this, economyManager);

        statusLogger = new KTStatusLogger(this, 26511, economyManager);
        statusLogger.logEnable();

        if (isFolia()) {
            SchedulerWrapper.runTaskLater(this, () -> {
                TempBlockStorage.hasBlocksToRestore()
                        .thenCompose(hasBlocks -> {
                            if (hasBlocks) {
                                getLogger().info("Found temporary blocks from previous session, restoring...");
                                return TempBlockStorage.printRestoreStats()
                                        .thenCompose(v -> TempBlockStorage.removeAllTempBlocks())
                                        .thenRun(() -> getLogger().info("Temporary blocks restoration completed!"));
                            } else {
                                getLogger().info("No temporary blocks to restore");
                                return CompletableFuture.completedFuture(null);
                            }
                        })
                        .exceptionally(throwable -> {
                            getLogger().severe("Error during temp blocks operation: " + throwable.getMessage());
                            throwable.printStackTrace();
                            return null;
                        });
            }, 0L);
        } else {
            TempBlockStorage.removeAllTempBlocks();
        }

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
        if (!isFolia()) {
            TempBlockStorage.forceFlushAll();
        }

        if (eventManager != null) {
            eventManager.stopAllEvents();
        }
        if (economyManager != null) {
            economyManager.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
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

    private void cleanupObsoleteEffects() {
        SchedulerWrapper.runTaskAsynchronously(this, () -> {
            try {
                java.util.Set<String> validEffects = new java.util.HashSet<>(
                        com.monkey.kt.effects.KillEffectFactory.getRegisteredEffects()
                );

                java.util.Map<java.util.UUID, String> allEffects =
                        com.monkey.kt.storage.EffectStorage.getAllEffects();

                int removed = 0;
                for (java.util.Map.Entry<java.util.UUID, String> entry : allEffects.entrySet()) {
                    String effectName = entry.getValue();
                    if (!validEffects.contains(effectName.toLowerCase())) {
                        com.monkey.kt.storage.EffectStorage.removeEffect(entry.getKey());
                        removed++;
                        getLogger().info("Removed obsolete effect '" + effectName +
                                "' from player " + entry.getKey());
                    }
                }

                if (removed > 0) {
                    getLogger().info("Cleaned up " + removed + " obsolete effects from database");
                }

                if (economyManager != null && economyManager.getInternalEconomy() != null) {
                    cleanupObsoletePurchases(validEffects);
                }

            } catch (Exception e) {
                getLogger().warning("Error cleaning up obsolete effects: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void cleanupObsoletePurchases(java.util.Set<String> validEffects) {
        try {
            com.monkey.kt.economy.storage.KillCoinsStorage storage =
                    economyManager.getInternalEconomy().getStorage();

            java.util.Map<java.util.UUID, java.util.Set<String>> allPurchases =
                    storage.getAllPurchases();

            databaseManager.getExecutor().executeTransaction(connection -> {
                String deleteSql = "DELETE FROM killcoins_purchases WHERE effect = ?";
                try (java.sql.PreparedStatement ps = connection.prepareStatement(deleteSql)) {

                    int totalRemoved = 0;
                    for (java.util.Map.Entry<java.util.UUID, java.util.Set<String>> entry : allPurchases.entrySet()) {
                        java.util.Set<String> playerPurchases = entry.getValue();

                        for (String effectName : playerPurchases) {
                            if (!validEffects.contains(effectName.toLowerCase())) {
                                ps.setString(1, effectName);
                                int removed = ps.executeUpdate();
                                totalRemoved += removed;

                                getLogger().info("Removed obsolete purchase '" + effectName +
                                        "' from player " + entry.getKey());
                            }
                        }
                    }

                    if (totalRemoved > 0) {
                        getLogger().info("Cleaned up " + totalRemoved +
                                " obsolete purchases from database");
                    }

                } catch (java.sql.SQLException e) {
                    throw e;
                }
            });

            storage.getAllPurchases().clear();

        } catch (Exception e) {
            getLogger().warning("Error cleaning up obsolete purchases: " + e.getMessage());
            e.printStackTrace();
        }
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
        return economyManager != null ? economyManager.getInternalEconomy() : null;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    public void setEconomyManager(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
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
    public static boolean isFolia() {
        return SchedulerWrapper.isFolia();
    }
    public CustomEffectLoader getCustomEffectLoader() {
        return customEffectLoader;
    }
}