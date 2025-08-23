package com.monkey.kt.utils.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SchedulerWrapper {

    private static boolean isFolia = false;
    private static boolean initialized = false;

    private static Object globalRegionScheduler = null;
    private static Object regionScheduler = null;
    private static Object asyncScheduler = null;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
            regionScheduler = Bukkit.getRegionScheduler();
            asyncScheduler = Bukkit.getAsyncScheduler();
            isFolia = true;
            Bukkit.getLogger().info("[KT SchedulerWrapper] Folia detected — use of Folia schedulers enabled");
        } catch (Throwable t) {
            isFolia = false;
            Bukkit.getLogger().info("[KT SchedulerWrapper] Using Bukkit/Paper scheduler (Folia not detected)");
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static ScheduledTask runTask(Plugin plugin, Runnable runnable) {
        return runTaskLater(plugin, runnable, 1L);
    }

    public static ScheduledTask runTaskLater(Plugin plugin, Runnable runnable, long delayTicks) {
        if (!initialized) initialize();

        try {
            if (isFolia && globalRegionScheduler != null) {
                if (delayTicks <= 0) delayTicks = 1L;
                Object handle = globalRegionScheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Consumer.class, long.class)
                        .invoke(globalRegionScheduler, plugin, (Consumer) (task) -> safeRun(runnable), delayTicks);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
            return new BukkitScheduledTask(task);
        }
    }

    public static ScheduledTask runTaskTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (!initialized) initialize();

        try {
            if (isFolia && globalRegionScheduler != null) {
                if (delayTicks <= 0) delayTicks = 1L;
                Object handle = globalRegionScheduler.getClass()
                        .getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class)
                        .invoke(globalRegionScheduler, plugin, (Consumer) (task) -> safeRun(runnable), delayTicks, periodTicks);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
            return new BukkitScheduledTask(task);
        }
    }

    public static ScheduledTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        if (!initialized) initialize();

        try {
            if (isFolia && asyncScheduler != null) {
                Object handle = asyncScheduler.getClass()
                        .getMethod("runNow", Plugin.class, Consumer.class)
                        .invoke(asyncScheduler, plugin, (Consumer) (task) -> safeRun(runnable));
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            return new BukkitScheduledTask(task);
        }
    }

    public static ScheduledTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delayTicks) {
        if (!initialized) initialize();

        try {
            if (isFolia && asyncScheduler != null) {
                if (delayTicks <= 0) delayTicks = 1L;
                long delayMillis = delayTicks * 50L;
                Object handle = asyncScheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class)
                        .invoke(asyncScheduler, plugin, (Consumer) (task) -> safeRun(runnable), delayMillis, TimeUnit.MILLISECONDS);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks);
            return new BukkitScheduledTask(task);
        }
    }

    public static ScheduledTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (!initialized) initialize();

        try {
            if (isFolia && asyncScheduler != null) {
                if (delayTicks <= 0) delayTicks = 1L;
                long delayMillis = delayTicks * 50L;
                long periodMillis = periodTicks * 50L;
                Object handle = asyncScheduler.getClass()
                        .getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class)
                        .invoke(asyncScheduler, plugin, (Consumer) (task) -> safeRun(runnable),
                                delayMillis, periodMillis, TimeUnit.MILLISECONDS);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
            return new BukkitScheduledTask(task);
        }
    }

    public static ScheduledTask runTaskAtLocation(Plugin plugin, Runnable runnable, Location location, long delayTicks) {
        if (!initialized) initialize();

        try {
            if (isFolia && location != null && location.getWorld() != null && regionScheduler != null) {
                if (delayTicks <= 0) delayTicks = 1L;
                Object handle = regionScheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Location.class, Consumer.class, long.class)
                        .invoke(regionScheduler, plugin, location, (Consumer) (task) -> safeRun(runnable), delayTicks);
                return new FoliaScheduledTask(handle);
            } else {
                return runTaskLater(plugin, runnable, delayTicks);
            }
        } catch (Throwable t) {
            return runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public static ScheduledTask runTaskTimerAtLocation(Plugin plugin, Runnable runnable, Location location, long delayTicks, long periodTicks) {
        if (!initialized) initialize();

        try {
            if (isFolia && location != null && location.getWorld() != null && regionScheduler != null) {
                if (delayTicks <= 0) delayTicks = 1L;
                Object handle = regionScheduler.getClass()
                        .getMethod("runAtFixedRate", Plugin.class, Location.class, Consumer.class, long.class, long.class)
                        .invoke(regionScheduler, plugin, location, (Consumer) (task) -> safeRun(runnable), delayTicks, periodTicks);
                return new FoliaScheduledTask(handle);
            } else if (isFolia && globalRegionScheduler != null) {
                if (delayTicks <= 0) delayTicks = 1L;
                Object handle = globalRegionScheduler.getClass()
                        .getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class)
                        .invoke(globalRegionScheduler, plugin, (Consumer) (task) -> safeRun(runnable), delayTicks, periodTicks);
                return new FoliaScheduledTask(handle);
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
                return new BukkitScheduledTask(task);
            }
        } catch (Throwable t) {
            if (isFolia) {
                t.printStackTrace();
                return () -> {};
            } else {
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
                return new BukkitScheduledTask(task);
            }
        }
    }

    public static ScheduledTask runTaskTimerAtEntity(Plugin plugin, Runnable runnable, Entity entity, long delayTicks, long periodTicks) {
        if (!initialized) initialize();

        try {
            if (isFolia && entity != null) {
                if (delayTicks <= 0) delayTicks = 1L;
                Object entityScheduler = entity.getClass().getMethod("getScheduler").invoke(entity);

                try {
                    Object handle = entityScheduler.getClass()
                            .getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class)
                            .invoke(entityScheduler, plugin, (Consumer) (task) -> safeRun(runnable), delayTicks, periodTicks);
                    return new FoliaScheduledTask(handle);
                } catch (NoSuchMethodException e) {
                    Object handle = entityScheduler.getClass()
                            .getMethod("runAtFixedRate", Plugin.class, Consumer.class, Consumer.class, long.class, long.class)
                            .invoke(entityScheduler, plugin,
                                    (Consumer) (task) -> safeRun(runnable),
                                    (Consumer) (task) -> {},
                                    delayTicks, periodTicks);
                    return new FoliaScheduledTask(handle);
                }
            } else {
                return runTaskTimer(plugin, runnable, delayTicks, periodTicks);
            }
        } catch (Throwable t) {
            return runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        }
    }

    public interface ScheduledTask {
        void cancel();
    }

    private static class BukkitScheduledTask implements ScheduledTask {
        private final BukkitTask task;
        public BukkitScheduledTask(BukkitTask task) { this.task = task; }
        @Override
        public void cancel() {
            if (task != null) task.cancel();
        }
    }

    private static class FoliaScheduledTask implements ScheduledTask {
        private final Object handle;
        public FoliaScheduledTask(Object handle) { this.handle = handle; }
        @Override
        public void cancel() {
            if (handle == null) return;
            try {
                handle.getClass().getMethod("cancel").invoke(handle);
            } catch (Throwable t) {
            }
        }
    }

    public static void safeRun(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void safeCancelTask(Object taskObj) {
        if (taskObj == null) return;

        try {
            if (taskObj instanceof ScheduledTask) {
                ((ScheduledTask) taskObj).cancel();
            } else if (taskObj instanceof BukkitTask) {
                ((BukkitTask) taskObj).cancel();
            }
        } catch (Exception e) {
        }
    }
}