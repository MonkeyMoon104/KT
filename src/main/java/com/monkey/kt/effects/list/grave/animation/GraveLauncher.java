package com.monkey.kt.effects.list.grave.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.helper.BlockStateHolder;
import com.monkey.kt.effects.list.grave.animation.util.structure.placer.GravePlacer;
import com.monkey.kt.storage.TempBlockStorage;
import com.monkey.kt.utils.SensitiveBlockUtils;
import com.monkey.kt.utils.WorldGuardUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;

public class GraveLauncher {

    private static final int RESTORE_TICKS = 20 * 5;

    public static void launch(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null)
            return;

        int y = world.getHighestBlockYAt(center.getBlockX(), center.getBlockZ());
        Location surface = new Location(world, center.getBlockX(), y, center.getBlockZ());

        BlockStateHolder holder = new BlockStateHolder();

        BlockFace facing = yawToFace(killer.getLocation().getYaw());

        Location crossBase = surface.clone().add(0, 1, 0);
        buildFenceCross(holder, crossBase, facing);
        placeWallSignWithName(holder, crossBase.clone().add(0, 1, 0), facing, killer.getName());

        Location headBlockLoc = crossBase.clone().add(facing.getModX(), -1, facing.getModZ());
        placeHeadOnGround(holder, headBlockLoc, killer, facing);

        replaceGroundLineCoarseDirt(holder, headBlockLoc, facing);

        GravePlacer placer = new GravePlacer(plugin);
        placer.randomlyPlaceCoarseDirt(surface, 12, holder, 1, true);

        world.playSound(surface, Sound.BLOCK_WOOD_PLACE, 1f, 1f);
        world.playSound(surface, Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.8f);

        new BukkitRunnable() {
            @Override
            public void run() {
                restoreAll(holder);
            }
        }.runTaskLater(plugin, RESTORE_TICKS);
    }

    private static void buildFenceCross(BlockStateHolder holder, Location base, BlockFace facing) {
        for (int dy = 0; dy < 3; dy++) {
            setBlockSafely(holder, base.clone().add(0, dy, 0).getBlock(), Material.OAK_FENCE);
        }

        BlockFace left, right;
        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            left = BlockFace.WEST;
            right = BlockFace.EAST;
        } else {
            left = BlockFace.NORTH;
            right = BlockFace.SOUTH;
        }

        setBlockSafely(holder, base.clone().add(left.getModX(), 1, left.getModZ()).getBlock(), Material.OAK_FENCE);
        setBlockSafely(holder, base.clone().add(right.getModX(), 1, right.getModZ()).getBlock(), Material.OAK_FENCE);
    }

    private static void placeWallSignWithName(BlockStateHolder holder, Location midFence, BlockFace facing,
            String killerName) {
        Location signLoc = midFence.clone().add(facing.getModX(), 0, facing.getModZ());
        Block signBlock = signLoc.getBlock();

        setBlockTypeRemember(holder, signBlock, Material.OAK_WALL_SIGN);

        BlockData data = signBlock.getBlockData();
        if (data instanceof WallSign) {
            WallSign wall = (WallSign) data;
            wall.setFacing(facing);
            signBlock.setBlockData(wall, false);
        }

        if (signBlock.getState() instanceof Sign) {
            Sign sign = (Sign) signBlock.getState();
            sign.getSide(Side.FRONT).line(0, Component.text("R.I.P."));
            sign.getSide(Side.FRONT).line(1, Component.text(killerName));
            sign.update();
        }

    }

    private static void placeHeadOnGround(BlockStateHolder holder, Location headBlockLoc, Player killer,
            BlockFace facing) {
        headBlockLoc.add(0, 1, 0);
        Block headBlock = headBlockLoc.getBlock();

        setBlockTypeRemember(holder, headBlock, Material.PLAYER_HEAD);

        if (headBlock.getState() instanceof Skull) {
            Skull skull = (Skull) headBlock.getState();
            skull.setOwningPlayer(killer);
            skull.setRotation(facing);
            skull.update(true, false);
        }

    }

    private static void replaceGroundLineCoarseDirt(BlockStateHolder holder, Location headBlockLoc, BlockFace facing) {
        Block underHead = headBlockLoc.clone().add(0, -1, 0).getBlock();
        Block front1 = underHead.getRelative(facing);

        setGroundToCoarseDirt(holder, underHead);
        setGroundToCoarseDirt(holder, front1);

        Block flowerBlock = front1.getRelative(BlockFace.UP);
        setBlockSafely(holder, flowerBlock, Material.POPPY);
    }

    private static void placeRandomCoarseDirt(BlockStateHolder holder, Location surface, int radius, int tries) {
        Random r = new Random(surface.getBlockX() * 73477L ^ surface.getBlockZ() * 91283L ^ surface.getBlockY());
        World w = surface.getWorld();
        for (int i = 0; i < tries; i++) {
            int dx = r.nextInt(radius * 2 + 1) - radius;
            int dz = r.nextInt(radius * 2 + 1) - radius;
            int x = surface.getBlockX() + dx;
            int z = surface.getBlockZ() + dz;
            int y = w.getHighestBlockYAt(x, z);
            Block ground = new Location(w, x, y - 1, z).getBlock();
            setGroundToCoarseDirt(holder, ground);
        }
        TempBlockStorage.flush();
    }

    private static void restoreAll(BlockStateHolder holder) {
        for (Map.Entry<Location, Material> e : holder.originalBlocks.entrySet()) {
            Location loc = e.getKey();
            Material original = e.getValue();
            Block b = loc.getBlock();
            WorldGuardUtils.runWithWorldGuardBypass(loc, () -> b.setType(original, false));
            TempBlockStorage.removeTempBlock(loc);
        }
        holder.clear();
        TempBlockStorage.flush();
    }

    private static BlockFace yawToFace(float yaw) {
        float rot = (yaw % 360 + 360) % 360;
        if (rot >= 45 && rot < 135)
            return BlockFace.WEST;
        if (rot >= 135 && rot < 225)
            return BlockFace.NORTH;
        if (rot >= 225 && rot < 315)
            return BlockFace.EAST;
        return BlockFace.SOUTH;
    }

    private static boolean canPlaceHere(Block block) {
        if (SensitiveBlockUtils.isSensitive(block))
            return false;
        return true;
    }

    private static void setBlockSafely(BlockStateHolder holder, Block block, Material type) {
        if (SensitiveBlockUtils.isSensitive(block))
            return;
        setBlockTypeRemember(holder, block, type);
    }

    private static void setBlockTypeRemember(BlockStateHolder holder, Block block, Material newType) {
        Location loc = block.getLocation();
        if (!holder.originalBlocks.containsKey(loc)) {
            holder.originalBlocks.put(loc, block.getType());
        }
        WorldGuardUtils.runWithWorldGuardBypass(loc, () -> block.setType(newType, true));
        TempBlockStorage.saveTempBlock(loc, holder.originalBlocks.get(loc));
    }

    private static void setGroundToCoarseDirt(BlockStateHolder holder, Block groundBlock) {
        if (groundBlock == null)
            return;
        if (SensitiveBlockUtils.isSensitive(groundBlock))
            return;

        Material type = groundBlock.getType();
        if (type == Material.DIRT || type == Material.GRASS_BLOCK || type == Material.PODZOL
                || type == Material.COARSE_DIRT) {
            setBlockTypeRemember(holder, groundBlock, Material.COARSE_DIRT);
        }
    }

}
