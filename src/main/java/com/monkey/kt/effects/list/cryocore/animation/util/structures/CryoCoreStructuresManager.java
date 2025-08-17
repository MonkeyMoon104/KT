package com.monkey.kt.effects.list.cryocore.animation.util.structures;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.placer.IceSpikePlacer;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.placer.SnowBlockPlacer;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.helper.BlockStateHolder;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.placer.StructureRestorer;
import org.bukkit.*;

public class CryoCoreStructuresManager {

    private final KT plugin;
    private final SnowBlockPlacer snowBlockPlacer;
    private final IceSpikePlacer iceSpikePlacer;
    private final StructureRestorer structureRestorer;

    public CryoCoreStructuresManager(KT plugin) {
        this.plugin = plugin;
        this.snowBlockPlacer = new SnowBlockPlacer(plugin);
        this.iceSpikePlacer = new IceSpikePlacer(plugin);
        this.structureRestorer = new StructureRestorer(plugin);
    }

    public BlockStateHolder createBlockStateHolder() {
        return new BlockStateHolder();
    }

    public void randomlyPlaceSnowBlocks(Location center, int radius, BlockStateHolder holder, int batch, boolean allowStructure) {
        snowBlockPlacer.randomlyPlaceSnowBlocks(center, radius, holder, batch, allowStructure);
    }

    public void restoreGround(BlockStateHolder holder) {
        structureRestorer.restoreGround(holder);
    }

    public void spawnLargeIceSpikesBorder(Location center, int radius, BlockStateHolder holder, boolean allowStructure) {
        iceSpikePlacer.spawnLargeIceSpikesBorder(center, radius, holder, allowStructure);
    }
}