package com.monkey.kt.utils.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SchedulerWrapper {

    private static boolean isFolia = false;

    private static Object globalRegionScheduler = null;
    private static Object regionScheduler = null;
    private static Object asyncScheduler = null;

    private static Method globalRunAtFixedRateMethod = null;
    private static Method globalRunDelayedMethod = null;

    private static Method regionRunAtFixedRateMethod = null;
    private static Method regionRunDelayedMethod = null;

    private static Method asyncRunAtFixedRateMethod = null;
    private static Method asyncRunDelayedMethod = null;
    private static Method asyncRunNowMethod = null;

    private static Method getEntitySchedulerMethod = null;
    private static Method entityRunAtFixedRateMethod = null;


    public static void initialize() {
        try {
            globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
            regionScheduler = Bukkit.getRegionScheduler();
            asyncScheduler = Bukkit.getAsyncScheduler();
            isFolia = (globalRegionScheduler != null && regionScheduler != null && asyncScheduler != null);
        } catch (Throwable t) {
            isFolia = false;
        }

        if (!isFolia) {
            Bukkit.getLogger().info("[SchedulerWrapper] Using Bukkit/Paper scheduler (Folia non rilevato).");
            return;
        }

        try {
            Class<?> consumerClass = Consumer.class;

            Class<?> globalClass = globalRegionScheduler.getClass();
            globalRunAtFixedRateMethod = globalClass.getMethod(
                    "runAtFixedRate", Plugin.class, consumerClass, long.class, long.class);
            globalRunDelayedMethod = globalClass.getMethod(
                    "runDelayed", Plugin.class, consumerClass, long.class);

            Class<?> regionClass = regionScheduler.getClass();
            regionRunAtFixedRateMethod = regionClass.getMethod(
                    "runAtFixedRate", Plugin.class, Location.class, consumerClass, long.class, long.class);
            regionRunDelayedMethod = regionClass.getMethod(
                    "runDelayed", Plugin.class, Location.class, consumerClass, long.class);

            Class<?> asyncClass = asyncScheduler.getClass();
            asyncRunAtFixedRateMethod = asyncClass.getMethod(
                    "runAtFixedRate", Plugin.class, consumerClass, long.class, long.class, TimeUnit.class);
            asyncRunDelayedMethod = asyncClass.getMethod(
                    "runDelayed", Plugin.class, consumerClass, long.class, TimeUnit.class);
            asyncRunNowMethod = asyncClass.getMethod(
                    "runNow", Plugin.class, consumerClass);

            getEntitySchedulerMethod = Entity.class.getMethod("getScheduler");
            try {
                Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                entityRunAtFixedRateMethod = entitySchedulerClass.getMethod(
                        "runAtFixedRate", Plugin.class, consumerClass, long.class, long.class);
            } catch (NoSuchMethodException nsme) {
                try {
                    Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                    entityRunAtFixedRateMethod = entitySchedulerClass.getMethod(
                            "runAtFixedRate", Plugin.class, consumerClass, consumerClass, long.class, long.class);
                } catch (Throwable t) {
                    entityRunAtFixedRateMethod = null;
                }
            }
            Bukkit.getLogger().info("[SchedulerWrapper] Folia rilevato — uso degli scheduler Folia abilitato.");
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[SchedulerWrapper] Folia rilevato ma non tutte le firme sono disponibili: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static ScheduledTask runTask(Plugin plugin, Runnable runnable) {
        return runTaskLater(plugin, runnable, 0L);
    }

    public static ScheduledTask runTaskLater(Plugin plugin, Runnable runnable, long delayTicks) {
        try {
            if (isFolia && globalRunDelayedMethod != null) {
                Object handle = globalRunDelayedMethod.invoke(
                        globalRegionScheduler, plugin, (Consumer<Object>) (task) -> safeRun(runnable), delayTicks);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static ScheduledTask runTaskTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        try {
            if (isFolia && globalRunAtFixedRateMethod != null) {
                Object handle = globalRunAtFixedRateMethod.invoke(
                        globalRegionScheduler, plugin, (Consumer<Object>) (task) -> safeRun(runnable), delayTicks, periodTicks);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }


    public static ScheduledTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        try {
            if (isFolia && asyncRunNowMethod != null) {
                Object handle = asyncRunNowMethod.invoke(
                        asyncScheduler, plugin, (Consumer<Object>) (task) -> safeRun(runnable));
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static ScheduledTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delayTicks) {
        try {
            if (isFolia && asyncRunDelayedMethod != null) {
                long delayMillis = delayTicks * 50L;
                Object handle = asyncRunDelayedMethod.invoke(
                        asyncScheduler, plugin, (Consumer<Object>) (task) -> safeRun(runnable), delayMillis, TimeUnit.MILLISECONDS);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static ScheduledTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        try {
            if (isFolia && asyncRunAtFixedRateMethod != null) {
                long delayMillis = delayTicks * 50L;
                long periodMillis = periodTicks * 50L;
                Object handle = asyncRunAtFixedRateMethod.invoke(
                        asyncScheduler, plugin, (Consumer<Object>) (task) -> safeRun(runnable),
                        delayMillis, periodMillis, TimeUnit.MILLISECONDS);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }


    public static ScheduledTask runTaskTimerAtLocation(Plugin plugin, Runnable runnable, Location location, long delayTicks, long periodTicks) {
        try {
            if (isFolia && location != null && location.getWorld() != null && regionRunAtFixedRateMethod != null) {
                Object handle = regionRunAtFixedRateMethod.invoke(
                        regionScheduler, plugin, location, (Consumer<Object>) (task) -> safeRun(runnable), delayTicks, periodTicks);
                return new FoliaScheduledTask(handle);
            } else {
                return runTaskTimer(plugin, runnable, delayTicks, periodTicks);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        }
    }

    public static ScheduledTask runTaskTimerAtEntity(Plugin plugin, Runnable runnable, Entity entity, long delayTicks, long periodTicks) {
        try {
            if (isFolia && entity != null && getEntitySchedulerMethod != null && entityRunAtFixedRateMethod != null) {
                Object entityScheduler = getEntitySchedulerMethod.invoke(entity);

                Object handle;
                Class<?>[] params = entityRunAtFixedRateMethod.getParameterTypes();
                if (params.length == 4) {
                    handle = entityRunAtFixedRateMethod.invoke(
                            entityScheduler, plugin, (Consumer<Object>) (task) -> safeRun(runnable), delayTicks, periodTicks);
                } else {
                    handle = entityRunAtFixedRateMethod.invoke(
                            entityScheduler,
                            plugin,
                            (Consumer<Object>) (task) -> safeRun(runnable),
                            (Consumer<Object>) (task) -> {},
                            delayTicks,
                            periodTicks
                    );
                }
                return new FoliaScheduledTask(handle);
            } else {
                return runTaskTimer(plugin, runnable, delayTicks, periodTicks);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        }
    }


    public interface ScheduledTask {
        void cancel();
    }

    private static class BukkitScheduledTask implements ScheduledTask {
        private final BukkitTask task;
        public BukkitScheduledTask(BukkitTask task) { this.task = task; }
        @Override public void cancel() { if (task != null) task.cancel(); }
    }

    private static class FoliaScheduledTask implements ScheduledTask {
        private final Object handle;
        public FoliaScheduledTask(Object handle) { this.handle = handle; }
        @Override
        public void cancel() {
            if (handle == null) return;
            try {
                Method m = handle.getClass().getMethod("cancel");
                m.invoke(handle);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void safeRun(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}