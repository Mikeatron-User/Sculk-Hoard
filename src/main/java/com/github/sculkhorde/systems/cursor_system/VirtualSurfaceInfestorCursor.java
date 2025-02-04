package com.github.sculkhorde.systems.cursor_system;

import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

import static com.github.sculkhorde.util.BlockAlgorithms.isExposedToInfestationWardBlock;

public class VirtualSurfaceInfestorCursor extends VirtualCursor{

    public VirtualSurfaceInfestorCursor(Level level)
    {
        super(level);
        cursorType = CursorType.INFESTOR;
    }


    /**
     * Returns true if the block is considered a target.
     * @param pos the block position
     * @return true if the block is considered a target
     */
    @Override
    protected boolean isTarget(BlockPos pos)
    {
        return BlockInfestationSystem.isInfectable((ServerLevel) getLevel(), pos);
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    @Override
    protected void transformBlock(BlockPos pos)
    {
        BlockInfestationSystem.tryToInfestBlock((ServerLevel) getLevel(), pos);

        // Get all infector cursor entities in area and kill them
        Predicate<CursorSurfacePurifierEntity> isCursor = Objects::nonNull;
        AABB searchBox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(getBlockPosition().getCenter(),5);
        List<CursorSurfacePurifierEntity> cursors = getLevel().getEntitiesOfClass(CursorSurfacePurifierEntity.class, searchBox, isCursor);
        for(CursorSurfacePurifierEntity cursor : cursors)
        {
            // Essentially kill it by taking away it's transformations.
            cursor.setMaxTransformations(0);
            setToBeDeleted();
            break;
        }
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return true;
        }
        else if(SculkHorde.savedData.isHordeDefeated())
        {
            return true;
        }
        else if(state.isAir())
        {
            return true;
        }
        // If we detect fluid
        else if(!state.getFluidState().isEmpty())
        {
            // If its water, its only obstructed if its the water source block or flowing water block
            if(state.getFluidState().is(Fluids.WATER) && state.is(Blocks.WATER))
            {
                return true;
            }

            if(!state.getFluidState().is(Fluids.WATER))
            {
                return true;
            }
        }
        else if(isExposedToInfestationWardBlock((ServerLevel) getLevel(), pos))
        {
            return true;
        }
        else if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }

        // Check if block is not beyond world border
        if(!getLevel().isInWorldBounds(pos))
        {
            return true;
        }

        // This is to prevent the entity from getting stuck in a loop
        if(visitedPositions.containsKey(pos.asLong()))
        {
            return true;
        }

        boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir((ServerLevel) getLevel(), pos);
        boolean isBlockNotSculkArachnoid = !state.is(ModBlocks.SCULK_ARACHNOID.get());
        boolean isBlockNotSculkDuraMatter = !state.is(ModBlocks.SCULK_DURA_MATTER.get());

        if(isBlockNotExposedToAir && isBlockNotSculkArachnoid && isBlockNotSculkDuraMatter)
        {
            return true;
        }

        return false;
    }

    @Override
    protected void spawnParticleEffects()
    {
        Random random = new Random();
        float maxOffset = 2;
        float randomXOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomYOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomZOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        getLevel().addParticle(
                ParticleTypes.SCULK_SOUL,
                getBlockPosition().getX() + randomXOffset,
                getBlockPosition().getY() + randomYOffset,
                getBlockPosition().getZ() + randomZOffset,
                randomXOffset * 0.1,
                randomYOffset * 0.1,
                randomZOffset * 0.1);
    }

}

