package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;


public class BeeColonyCoreBlockEntity extends StructureCoreBlockEntity
{

    /**
     * The Constructor that takes in properties
     * @param blockPos The Position
     * @param blockState The Properties
     */
    public BeeColonyCoreBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.BEE_COLONY_CORE_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    protected BlockState getBlockToConvertToAfterBuilding()
    {
        return Blocks.SCULK.defaultBlockState();
    }

    @Override
    protected void loadStructureVariants()
    {
        addStructureVariant("sculkhorde:bee_colony");
    }

}
