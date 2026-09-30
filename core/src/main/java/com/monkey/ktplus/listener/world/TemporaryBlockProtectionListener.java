package com.monkey.ktplus.listener.world;

import com.monkey.ktplus.effects.runtime.block.TemporaryBlockService;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;

public final class TemporaryBlockProtectionListener implements Listener {
    private final TemporaryBlockService temporaryBlocks;

    public TemporaryBlockProtectionListener(TemporaryBlockService temporaryBlocks) {
        this.temporaryBlocks = Objects.requireNonNull(temporaryBlocks, "temporaryBlocks");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(BlockDamageEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        filterTemp(event.blockList());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        filterTemp(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFade(BlockFadeEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFromTo(BlockFromToEvent event) {
        if (temporaryBlocks.isTemporary(event.getToBlock()) || temporaryBlocks.isTemporary(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChange(EntityChangeBlockEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block target = event.getBlockClicked().getRelative(event.getBlockFace());
        if (temporaryBlocks.isTemporary(target) || isControlledFire(target)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        cancelIfTemp(event, event.getBlockClicked());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (temporaryBlocks.isTemporary(block) || isControlledFire(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        cancelIfPistonAffectsTemp(event, event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        cancelIfPistonAffectsTemp(event, event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        if (temporaryBlocks.isTemporary(event.getSource()) || temporaryBlocks.isTemporary(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMultiPlace(BlockMultiPlaceEvent event) {
        if (temporaryBlocks.isTemporary(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        for (BlockState state : event.getReplacedBlockStates()) {
            if (temporaryBlocks.isTemporary(state.getBlock())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        if (temporaryBlocks.isTemporary(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        filterTempStates(event.getBlocks());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onForm(BlockFormEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityForm(EntityBlockFormEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent event) {
        if (temporaryBlocks.isTemporary(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        filterTempStates(event.getBlocks());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMoisture(MoistureChangeEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCauldron(CauldronLevelChangeEvent event) {
        cancelIfTemp(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        Block dispenser = event.getBlock();
        if (temporaryBlocks.isTemporary(dispenser)) {
            event.setCancelled(true);
            return;
        }
        if (dispenser.getBlockData() instanceof Directional directional) {
            Block target = dispenser.getRelative(directional.getFacing());
            if (temporaryBlocks.isTemporary(target) || isControlledFire(target)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        cancelIfTemp(event, event.getHarvestedBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        for (BlockState state : event.getBlocks()) {
            if (temporaryBlocks.isTemporary(state.getBlock())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakBlockEvent event) {
        if (temporaryBlocks.isTemporary(event.getBlock())) {
            event.getDrops().clear();
            temporaryBlocks.reassert(event.getBlock());
        }
    }

    private void cancelIfPistonAffectsTemp(BlockPistonEvent event, List<Block> moved, BlockFace direction) {
        if (anyTemp(moved)) {
            event.setCancelled(true);
            return;
        }
        for (Block block : moved) {
            if (temporaryBlocks.isTemporary(block.getRelative(direction))) {
                event.setCancelled(true);
                return;
            }
        }
        Block head = event.getBlock().getRelative(direction);
        if (temporaryBlocks.isTemporary(head)) {
            event.setCancelled(true);
        }
    }

    private void cancelIfTemp(Cancellable event, Block block) {
        if (temporaryBlocks.isTemporary(block)) {
            event.setCancelled(true);
        }
    }

    private boolean anyTemp(Iterable<Block> blocks) {
        for (Block block : blocks) {
            if (temporaryBlocks.isTemporary(block)) {
                return true;
            }
        }
        return false;
    }

    private void filterTemp(List<Block> blocks) {
        blocks.removeIf(temporaryBlocks::isTemporary);
    }

    private void filterTempStates(List<BlockState> states) {
        Iterator<BlockState> iterator = states.iterator();
        while (iterator.hasNext()) {
            if (temporaryBlocks.isTemporary(iterator.next().getBlock())) {
                iterator.remove();
            }
        }
    }

    private boolean isControlledFire(Block block) {
        Material type = block.getType();
        return (type == Material.FIRE || type == Material.SOUL_FIRE) && temporaryBlocks.isTemporary(block);
    }
}
