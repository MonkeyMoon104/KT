package com.monkey.ktplus.effects.runtime.block;

import com.monkey.ktplus.scheduler.PlatformScheduler;
import com.monkey.ktplus.storage.repository.TemporaryBlockRepository;
import com.monkey.ktplus.storage.repository.TemporaryBlockRepository.StoredTemporaryBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.Nullable;

public final class TemporaryBlockService {
    private final Map<String, TemporaryBlockChange> active = new ConcurrentHashMap<String, TemporaryBlockChange>();
    private final @Nullable TemporaryBlockRepository repository;

    public TemporaryBlockService() {
        this(null);
    }

    public TemporaryBlockService(@Nullable TemporaryBlockRepository repository) {
        this.repository = repository;
    }

    public void restorePersisted() {
        if (repository == null) {
            return;
        }
        for (StoredTemporaryBlock stored : repository.loadAll()) {
            tryRestoreStored(stored);
        }
    }

    public void restoreWorld(World world) {
        Objects.requireNonNull(world, "world");
        if (repository == null) {
            return;
        }
        for (StoredTemporaryBlock stored : repository.loadAll()) {
            if (!world.getName().equals(stored.worldName())) {
                continue;
            }
            tryRestoreStored(stored);
        }
    }

    public TemporaryBlockChange change(Block block, Material material) {
        return change(block, material, false);
    }

    public TemporaryBlockChange change(Block block, Material material, boolean applyPhysics) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(material, "material");
        String key = key(block);
        TemporaryBlockChange existing = active.get(key);
        if (existing != null) {
            existing.setExpectedMaterial(material);
            block.setType(material, applyPhysics);
            return existing;
        }
        Material original = block.getType();
        String payload = PersistedBlockPayload.capture(block);
        TemporaryBlockChange change = new TemporaryBlockChange(key, block, block.getState(), material);
        active.put(key, change);
        if (repository != null) {
            repository.saveAsync(block.getLocation(), original.name(), payload);
        }
        block.setType(material, applyPhysics);
        return change;
    }

    public boolean isTemporary(Block block) {
        return active.containsKey(key(block));
    }

    public void restore(TemporaryBlockChange change) {
        Objects.requireNonNull(change, "change");
        if (active.remove(change.key()) != null) {
            change.restore();
            if (repository != null) {
                repository.removeAsync(change.block().getLocation());
            }
        }
    }

    public void restoreAll() {
        Collection<TemporaryBlockChange> changes = active.values();
        for (TemporaryBlockChange change : changes) {
            change.restore();
            if (repository != null) {
                repository.remove(change.block().getLocation());
            }
        }
        active.clear();
    }

    public void reassert(Block block) {
        Objects.requireNonNull(block, "block");
        TemporaryBlockChange change = active.get(key(block));
        if (change == null) {
            return;
        }
        reassert(change, change.expectedMaterial());
    }

    public void reconcileLoaded(@Nullable PlatformScheduler scheduler) {
        if (active.isEmpty()) {
            return;
        }
        List<TemporaryBlockChange> snapshot = new ArrayList<TemporaryBlockChange>(active.values());
        for (TemporaryBlockChange change : snapshot) {
            Block block = change.block();
            World world = block.getWorld();
            if (!world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                continue;
            }
            Material expected = change.expectedMaterial();
            if (block.getType() == expected) {
                continue;
            }
            if (scheduler != null) {
                scheduler.run(block.getLocation(), () -> reassert(change, expected));
            } else {
                reassert(change, expected);
            }
        }
    }

    public void shutdown() {
        if (repository != null) {
            repository.drainAndShutdown();
        }
    }

    private void reassert(TemporaryBlockChange change, Material expected) {
        if (active.get(change.key()) != change) {
            return;
        }
        Block block = change.block();
        if (block.getType() != expected) {
            block.setType(expected, false);
        }
    }

    private void tryRestoreStored(StoredTemporaryBlock stored) {
        Location location = stored.toLocation();
        if (location == null) {
            return;
        }
        PersistedBlockPayload.restore(location.getBlock(), stored.material(), stored.blockData());
        if (repository != null) {
            repository.remove(location);
        }
    }

    private String key(Block block) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }
}
