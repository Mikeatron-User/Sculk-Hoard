package com.github.sculkhorde.systems.chunk_cursor_system;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ChunkCursorInfector extends ChunkCursorBase<ChunkCursorInfector> {

    public ChunkCursorInfector () {
        super();
    }

    public static ChunkCursorInfector of() {
        return new ChunkCursorInfector();
    }

    // Initialisations -------------------------------------------------------------------------------------------------
    @Override
    protected void initDefaults() {
        super.initDefaults();
        this.blocksPerTick(256)
                .doNotPlaceFeatures()
                .spawnSurfaceCursorsAtEnd()
                .enableAdjacentBlocks();

        this.fullDebug.enabled = false;
    }

    // Check Blocks ----------------------------------------------------------------------------------------------------
    @Override
    protected boolean isObstructed(ServerLevel serverLevel, BlockPos pos) {
        return !BlockAlgorithms.isExposedToAir(serverLevel, pos) || BlockAlgorithms.isExposedToInfestationWardBlock(serverLevel, pos);
    }

    @Override
    protected boolean canChange(ServerLevel serverLevel, BlockPos pos) {
        return BlockInfestationSystem.isInfectable(serverLevel, pos);
    }

    @Override
    protected boolean canConsume(ServerLevel serverLevel, BlockPos pos) {
        return serverLevel.getBlockState(pos).is(BlockTags.LEAVES);
    }


    // Change Blocks ---------------------------------------------------------------------------------------------------
    @Override
    protected void changeBlock(ServerLevel serverLevel, BlockPos pos) {
        ChunkCursorHelper.tryToInfestBlock(serverLevel, pos, !shouldPlaceFeatures());
    }

    @Override
    protected void consumeItems(ServerLevel serverLevel, AABB boundingBox) {
        List<Entity> entities = serverLevel.getEntities(null, boundingBox);

        int consumed = 0;
        int massToAdd = 0;

        for (Entity entity : entities) {
            if (entity instanceof ItemEntity item) {
                if (ModConfig.SERVER.isItemEdibleToCursors(item)) {
                    item.discard();
                    consumed++;
                    massToAdd += item.getItem().getCount();
                }
                else if (ComposterBlock.COMPOSTABLES.containsKey(item.getItem().getItem())) {
                    consumed++;
                    item.discard();
                }
            }
        }

        SculkHorde.savedData.addSculkAccumulatedMass(massToAdd);
        SculkHorde.statisticsData.addTotalMassFromInfestedCursorItemEating(massToAdd);

        fullDebug.info("Consumed " + consumed + " items | Generating " + massToAdd + " mass");
    }

}
