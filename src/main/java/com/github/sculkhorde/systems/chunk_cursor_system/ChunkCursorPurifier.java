package com.github.sculkhorde.systems.chunk_cursor_system;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ChunkCursorPurifier extends ChunkCursorBase<ChunkCursorPurifier> {

    public ChunkCursorPurifier() {
        super();
    }

    public static ChunkCursorPurifier of() {
        return new ChunkCursorPurifier();
    }

    // Initialisations -------------------------------------------------------------------------------------------------
    @Override
    protected void initDefaults() {
        super.initDefaults();
        this.blocksPerTick(512)
                .doNotPlaceFeatures()
                .disableAdjacentBlocks()
                .solidFill();

        this.fullDebug.enabled = false;
    }

    // Check Blocks ----------------------------------------------------------------------------------------------------

    BlockPos xzPos = null;
    int totalObstructions = 0;

    @Override
    protected boolean isObstructed(ServerLevel serverLevel, BlockPos pos) {
        if (xzPos == null || !(xzPos.getX() == pos.getX() && xzPos.getZ() == pos.getZ())) {
            xzPos = pos;
            totalObstructions = 0;
        }

        if (!BlockAlgorithms.isExposedToAir(serverLevel, pos)) {
            totalObstructions++;
        }

        return totalObstructions >= 3;
    }

    @Override
    protected boolean canChange(ServerLevel serverLevel, BlockPos pos) {
        return BlockInfestationSystem.isCurable(serverLevel, pos);
    }


    // Change Blocks ---------------------------------------------------------------------------------------------------
    @Override
    protected void changeBlock(ServerLevel serverLevel, BlockPos pos) {
        ChunkCursorHelper.tryToCureBlock(serverLevel, pos, !shouldPlaceFeatures());
    }

    @Override
    protected void consumeItems(ServerLevel serverLevel, AABB boundingBox) {
        List<Entity> entities = serverLevel.getEntities(null, boundingBox);

        int consumed = 0;

        for (Entity entity : entities) {
            if (entity instanceof ItemEntity item) {
                if (ModConfig.SERVER.isItemEdibleToCursors(item)) {
                    item.discard();
                    consumed++;
                }
                else if (ComposterBlock.COMPOSTABLES.containsKey(item.getItem().getItem())) {
                    consumed++;
                    item.discard();
                }
            }
        }

        fullDebug.info("Consumed " + consumed + " items");
    }

}
